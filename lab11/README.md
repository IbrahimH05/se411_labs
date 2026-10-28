# Lab 11 — Introduction to Threads

SE411, Fall 2026/2027. The double spending problem: ten accounts, ten thousand
concurrent transfers, and what happens to the money when nothing guards the balance.

## Files

| File | What it is |
| --- | --- |
| [`model/CryptoAccount.java`](src/main/java/edu/psu/se411/lab11_threads/model/CryptoAccount.java) | **Part 2 — the fix.** `synchronized` on both mutators |
| [`model/UnsafeCryptoAccount.java`](src/main/java/edu/psu/se411/lab11_threads/model/UnsafeCryptoAccount.java) | **Part 1 — the problem.** Same class with the keyword removed |
| [`model/Transaction.java`](src/main/java/edu/psu/se411/lab11_threads/model/Transaction.java) | A transfer; implements `Runnable` so it can be a thread body |
| [`App.java`](src/main/java/edu/psu/se411/lab11_threads/App.java) | Runs both scenarios back to back and totals the money |
| [`ConcurrencyTest.java`](src/test/java/edu/psu/se411/lab11_threads/ConcurrencyTest.java) | 11 tests |

## Running it

```bash
mvn clean package exec:java
```

Tested with Maven 3.9.16 and OpenJDK 26.0.2.1. `logs/` is gitignored.

---

## Why money disappears

`balance += amount` looks like one step. It is three:

```
read balance  →  add amount  →  write balance
```

Two threads can read the same starting value, each add their own amount to it, and
each write back. The second write overwrites the first, and that transfer is gone —
or, when the losing write is a withdrawal, money is **created**. Nothing in the
language stops this: the field is just memory, and a thread can be suspended between
any two of those three steps.

`synchronized` makes the whole read-modify-write atomic: a thread must hold the
account's monitor to run the method, so no other thread can be inside it on the same
object.

### Demonstrating it rather than describing it

`UnsafeCryptoAccount extends CryptoAccount` and overrides both mutators **without**
the keyword. That is legal Java — `synchronized` is not part of a method's signature,
so an override can drop it silently. Which is itself worth knowing:

> **Thread safety is not inherited.** Nothing in the type system carries the parent's
> guarantee down to a subclass, and nothing warns you when a subclass throws it away.

---

## Results

```text
10 accounts x 1000.00 riyals = 10000.00 riyals in the system

== WITHOUT synchronization ==
  before : 10000.00 riyals
  after  : 9971.87 riyals
  drift  : -28.13 riyals  (MONEY INVENTED OR DESTROYED)
  exact  : -28.129201594358  (a real loss)
  took   : 465 ms for 10000 threads

== WITH synchronization ==
  before : 10000.00 riyals
  after  : 10000.00 riyals
  drift  : -0.00 riyals  (money conserved)
  exact  : -0.000000000004  (floating-point residue only, not a lost transaction)
  took   : 433 ms for 10000 threads
```

The drift is **nondeterministic**, which is the signature of a race. Five consecutive
runs of the unsynchronised scenario:

| Run | Drift |
| --- | --- |
| 1 | −22.71 |
| 2 | +20.55 |
| 3 | +12.78 |
| 4 | +18.24 |
| 5 | +11.70 |

Never zero, never the same twice, and it goes **both ways** — a lost deposit destroys
money, a lost withdrawal invents it. Same code, same inputs, different answer each
time. That is why a race cannot be found by reading the output once.

The synchronised scenario conserved the total on every one of those runs.

### The `-0.00` that is not a bug

A first version of this program compared the total against `10000.0` with `==` and
reported the **synchronised** run as broken. It was not. Summing `double`s in a
different order gives a different last bit, so a correct run lands on
`10000.000000000004`, not `10000.0`.

The two effects are ten orders of magnitude apart, which is what makes them easy to
tell apart once you look:

| | Magnitude | Cause |
| --- | --- | --- |
| Race | `~1e+1` riyals | Lost updates — a real accounting error |
| Rounding | `~1e-12` riyals | `double` cannot represent most decimals exactly |

The program prints both a rounded and an exact drift, and compares against a `1e-6`
tolerance. Lab 13's handout makes the same point about money and `double`; here it
shows up as noise sitting underneath a genuine bug, which is exactly why mixing the
two is dangerous — a tolerance wide enough to swallow rounding can swallow a small
race too.

---

## Tests

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

| Group | Tests |
| --- | --- |
| Transfers | Money moves sender → receiver; an overdraw is refused and changes nothing; sender = receiver rejected; the handout's 3-argument constructor generates unique ids; negative amounts and balances rejected |
| Concurrency | `@RepeatedTest(5)` — the synchronised account conserves exactly 10,000 riyals across 2,000 concurrent transfers |

### What is deliberately not tested

There is **no test asserting that the unsynchronised version loses money.** Such a
test is flaky by construction: the interleaving that drops an update is likely, never
guaranteed, so the test would pass or fail on timing rather than on correctness. A
green run would prove nothing and a red one would be unactionable.

What is asserted instead is the half that must always hold — the synchronised account
conserves the total, every time, repeated. The race itself is evidence for the
README, gathered by running the program; it is not a regression test.

---

## Notes

**1. The handout says to synchronise "one method"; two are needed.** Guarding only
`addToBalance` leaves `retrieveFromBalance` able to interleave with it, and the total
still drifts. Both mutators are synchronised here, which also matches the instructor's
own solution shipped as Lab 12's starter code.

A genuine one-method answer exists — a single `static synchronized` transfer method,
one global lock for the whole system — and it is correct, but it serialises every
transfer in the system against every other. Per-account locking lets transfers between
different pairs of accounts proceed in parallel, which is the point of Lab 12.

**2. `getBalance()` is synchronised too.** Not for atomicity but for **visibility**:
without it, a reader thread may see a stale cached value rather than the latest write.
A balance that is correct in memory but invisible to the thread reading it is the
same bug wearing a different hat.

**3. The starter's initial balance was 10,000 per account.** The handout asks for ten
accounts holding 1,000 each, for 10,000 in the system; `initialBalance` is set to
`1000.0` to match the text.

**4. The handout's example calls `new Transaction(randomA, randomB, randomAmount)`** —
three arguments — while the starter's constructor takes an id first. A three-argument
constructor was added that generates the id from an `AtomicLong`, so the handout's
code compiles verbatim and the starter's four-argument form still works.

**5. Ten thousand real threads, not a pool.** The handout says "creates ten thousand
threads", so `new Thread(...)` is used rather than an executor. Thread pools arrive in
Lab 12 — and the ~460 ms it takes to start and join 10,000 OS threads is the reason
they exist.
