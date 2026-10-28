# Lab 12 — Threads and Locks

SE411, Fall 2026/2027. Fixed versus cached thread pools, replacing `synchronized`
with explicit locks, the deadlock that introduces, and resource ordering as the fix.

## Files

| File | What it is |
| --- | --- |
| [`model/CryptoAccount.java`](src/main/java/edu/psu/se411/lab12_threads/model/CryptoAccount.java) | `synchronized` replaced by a `ReentrantLock`; `Comparable` for resource ordering |
| [`model/Transaction.java`](src/main/java/edu/psu/se411/lab12_threads/model/Transaction.java) | Holds **both** locks for the whole transfer; lock order left to the subclass |
| [`model/NaiveTransaction.java`](src/main/java/edu/psu/se411/lab12_threads/model/NaiveTransaction.java) | Sender first, then receiver — **deadlocks** |
| [`model/OrderedTransaction.java`](src/main/java/edu/psu/se411/lab12_threads/model/OrderedTransaction.java) | Lowest account id first — **the fix** |
| [`Simulation.java`](src/main/java/edu/psu/se411/lab12_threads/Simulation.java) | The run itself, shared so the executor is the only variable |
| [`App.java`](src/main/java/edu/psu/se411/lab12_threads/App.java) | Main program 1 — fixed pool |
| [`AppCachedPool.java`](src/main/java/edu/psu/se411/lab12_threads/AppCachedPool.java) | Main program 2 — cached pool |
| [`DeadlockDemo.java`](src/main/java/edu/psu/se411/lab12_threads/DeadlockDemo.java) | Proves the deadlock, then proves the fix |
| [`LockingTest.java`](src/test/java/edu/psu/se411/lab12_threads/LockingTest.java) | 11 tests |

## Running it

```bash
mvn clean package exec:java
```

```bash
mvn exec:java -Dexec.mainClass=edu.psu.se411.lab12_threads.AppCachedPool
```

```bash
mvn exec:java -Dexec.mainClass=edu.psu.se411.lab12_threads.DeadlockDemo
```

Both simulations take `-Dsim.accounts=` and `-Dsim.transactions=`:

```bash
mvn exec:java -Dexec.mainClass=edu.psu.se411.lab12_threads.App -Dsim.accounts=1000 -Dsim.transactions=1000000
```

Tested with Maven 3.9.16 and OpenJDK 26.0.2.1 on 8 cores. `logs/` is gitignored.

---

## Part 1 — fixed versus cached thread pool

### What the starter does, and why it cannot scale

The starter creates `Executors.newFixedThreadPool(nbTransactions)` — **one thread per
transaction.** At a million transactions that asks the JVM for a million threads, each
with its own stack of around a megabyte. It runs out of memory long before it runs out
of work.

A fixed pool exists precisely to *cap* concurrency, so it is sized to the hardware.
`App` defaults to `Runtime.getRuntime().availableProcessors()` (8 here) and the size is
overridable with `-Dsim.poolsize=`.

### Measured, 1,000,000 transactions

```text
  fixed(8)        1000 accounts    1000000 txns      662 ms     1510574 txn/s  peak      8 threads  money conserved
  cached          1000 accounts    1000000 txns     4340 ms      230415 txn/s  peak     23 threads  money conserved
  fixed(8)          10 accounts    1000000 txns      743 ms     1345895 txn/s  peak      8 threads  money conserved
  cached            10 accounts    1000000 txns     4354 ms      229674 txn/s  peak     29 threads  money conserved
```

### What do you observe?

**1. The fixed pool is about 6.5× faster**, at both account counts. That is the
headline, and the reason is not the number of threads.

**2. The cost is in the queue, not the threads.** The two executors differ in how a
submitted task reaches a worker:

| | Queue | Cost per submission |
| --- | --- | --- |
| `newFixedThreadPool` | `LinkedBlockingQueue` (unbounded) | Enqueue and return immediately |
| `newCachedThreadPool` | `SynchronousQueue` | **Hand off directly to a thread** — the submitter waits for a worker to take it, or creates one |

A `SynchronousQueue` has no capacity at all. Every one of the million submissions is a
rendezvous between the submitting thread and a worker. That handoff, a million times
over, is what the extra ~3.7 seconds buys.

**3. Thread count stayed modest, which is the surprise.** The cached pool is unbounded
and will create a thread whenever no idle one is free — the textbook warning is that it
explodes. It peaked at **23–29 threads**, not thousands, because these transfers finish
in microseconds and a freed worker is always available to take the next task. The
danger is real but needs *slow* tasks to materialise.

**4. Account count matters less than expected, and only to the fixed pool.** Ten
accounts means ten locks shared by eight workers; a thousand accounts means contention
is rare. Three repeats each, discarding the first run as JIT warm-up:

| Accounts | fixed(8) median | cached |
| --- | --- | --- |
| 1000 | ~636 ms | ~4340 ms |
| 10 | ~745 ms | ~4354 ms |

Contention costs the fixed pool about **15%**. For the cached pool it is invisible —
its own submission overhead is so much larger than the lock contention that the
difference disappears into it. A bottleneck hides every smaller one behind it.

---

## Part 2 — replacing `synchronized` with locks

`synchronized` methods cannot express this transfer. Each one takes its monitor and
releases it on return, so `retrieveFromBalance` then `addToBalance` is **two** atomic
steps with a gap in the middle where the money exists in neither account. Lab 11 got
away with it because the two errors cancel in the total, but another thread really can
observe the gap.

An explicit `ReentrantLock` can be held across both operations, so the transfer becomes
atomic:

```java
first.lock();
try {
    second.lock();
    try {
        sender.retrieveFromBalance(amount);
        receiver.addToBalance(amount);
    } finally { second.unlock(); }
} finally { first.unlock(); }
```

The `try`/`finally` pairs are not decoration. A lock is not released at the end of a
block the way a monitor is — if an exception escapes, the lock is held forever and
every later thread blocks on it. There is a test asserting both locks are free after a
refused transfer.

### What could go wrong?

**Deadlock.** Two transfers in opposite directions over the same pair:

```
Thread 1:  A→B    locks A ... waits for B
Thread 2:  B→A    locks B ... waits for A
```

Neither can proceed and neither will give up what it holds. Confirmed by the JVM
itself rather than inferred from a hang:

```text

== NaiveTransaction  (sender first, then receiver) ==
  STILL RUNNING after 5 seconds - 400 transfer(s) stuck
  DEADLOCK CONFIRMED by ThreadMXBean: 400 threads in a lock cycle
    Thread-1 is BLOCKED on java.util.concurrent.locks.ReentrantLock$NonfairSync@6c8e9650 held by Thread-2
    Thread-2 is BLOCKED on java.util.concurrent.locks.ReentrantLock$NonfairSync@9ebdfd8 held by Thread-1

== OrderedTransaction (lowest account id first) ==
  all 400 transfers completed
```

`ThreadMXBean.findDeadlockedThreads()` asks the JVM to look for a cycle in the
wait-for graph. It names the two threads, the two locks, and who holds what — that is
proof, not a guess from a program that stopped printing.

### Making a rare bug reproducible

The first version of this demo **did not deadlock**: 400 transfers completed every
time. The window between taking the first lock and the second is a few nanoseconds
wide, so the fatal interleaving is possible but unlikely.

`Transaction.setLockWindowNanos(...)` parks briefly after the first lock, widening that
window to 2 ms. It is zero by default and therefore inert in the benchmarks. It does
not *create* the bug — the bug was always there — it only makes an already-present
race happen on demand.

That is the honest version of the lesson: a deadlock that shows up once a month in
production is the same bug as this one. The difference is only how wide the window is,
and the window gets wider under load, which is exactly when you least want it.

---

## The fix — resource ordering

Deadlock needs four conditions at once; the practical one to break is **circular
wait.** Impose a total order on the locks and make every thread acquire in that order:

```java
return sender.compareTo(receiver) <= 0
        ? new CryptoAccount[] { sender, receiver }
        : new CryptoAccount[] { receiver, sender };
```

Now a thread only ever waits for an account *greater* than the one it already holds.
"Greater than" is transitive and irreflexive, so a cycle cannot exist — the argument is
structural, not probabilistic. `OrderedTransaction` completed all 400 transfers with
the same 2 ms window that deadlocked the naive version.

**The order does not have to be sensible, only agreed.** `compareTo` is lexicographic
on the id, so `acc_10` sorts before `acc_2`. That is fine. What matters is that every
thread computes the same order, not that the order means anything.

**Ties have to be broken.** If two accounts shared an id, `compareTo` would return 0,
both threads could pick a different "first", and the cycle would be back. Identity hash
breaks the tie so the order stays total. There is a test for it.

---

## Tests

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

| Group | Tests |
| --- | --- |
| Transfers | Money moves and the total is conserved; an overdraw is refused and leaves both accounts untouched; **both locks are released even when the transfer is refused** |
| Resource ordering | Accounts have a total order; two accounts sharing an id still order consistently and antisymmetrically |
| Deadlock | `@RepeatedTest(3)` — opposing ordered transfers complete within 15 s **with the lock window widened to the value that reliably deadlocks the naive version** |
| Concurrency | `@RepeatedTest(3)` — 20,000 concurrent transfers conserve the total exactly |

The deadlock test is the useful one: it runs under the exact conditions that break
`NaiveTransaction`, so it would fail if someone "simplified" the ordering away. As in
Lab 11, there is no test asserting the naive version *does* deadlock — a test demanding
a race occur is flaky by construction.

---

## Notes

**1. The starter's pool sizing is left documented rather than reproduced.** The
comparison the handout asks for only means something between two pools that both
survive a million tasks.

**2. `-Dexec.mainClass` did not work until the pom was changed.** A `<mainClass>`
hard-coded in the plugin's `<configuration>` wins over the command-line property, so
the second main program was unreachable; the pom now reads `${exec.mainClass}` with
`App` as the default, leaving `clean package exec:java` unchanged.

**3. `getBalance()` takes the lock.** Reading a `double` is not atomic on every JVM,
and a lock-free read can also return a stale cached value. Correct in memory but
invisible to the reader is the same bug in a different costume.

**4. The deadlock demo's threads are daemons** and it ends with `System.exit(0)`.
Otherwise the deadlocked threads would keep the JVM alive forever and `mvn exec:java`
would never return — a demonstration that hangs the build is not a demonstration.

**5. This still uses `double` for money.** Lab 11's README covers why that is wrong;
the totals here are compared with a `1e-6` tolerance for the same reason.
