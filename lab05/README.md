# Lab 05 — Exception Handling

SE411, Fall 2026/2027. Custom checked exceptions, and an online wallet that
refuses to overdraw.

## Files

| File | Exercise | What it is |
| --- | --- | --- |
| [`exceptions/InvalidAgeException.java`](src/main/java/edu/psu/se411/exceptions/InvalidAgeException.java) | 1 | Custom checked exception, carries the rejected age |
| [`exceptions/InsufficientFundsException.java`](src/main/java/edu/psu/se411/exceptions/InsufficientFundsException.java) | 2 | Custom checked exception, carries requested / available / shortfall |
| [`model/Wallet.java`](src/main/java/edu/psu/se411/model/Wallet.java) | 2 | The wallet, and the `withdrawToBank` operation |
| [`model/BankAccount.java`](src/main/java/edu/psu/se411/model/BankAccount.java) | 2 | The withdrawal destination |
| [`App.java`](src/main/java/edu/psu/se411/App.java) | 1 + 2 | `validateAge` and the `main` that drives both exercises |
| [`ExceptionHandlingTest.java`](src/test/java/edu/psu/se411/ExceptionHandlingTest.java) | 1 + 2 | 9 tests proving the exception behaviour |

The two exception classes are alone in `edu.psu.se411.exceptions` — the handout
asks for a package containing nothing but exceptions, and keeping it that way means
one glance at the package tells you every failure mode the program has.

## Running it

The handout's sequence, using the `exec-maven-plugin`:

```bash
mvn clean package exec:java
```

```bash
mvn test
```

`exec:java` only runs the program *after* it has been packaged, which is why
`package` comes first — running `exec:java` on its own after a `clean` finds no
compiled classes. Tested with Maven 3.9.16 and OpenJDK 26.0.2.1.

---

## Exercise 1 — a custom checked exception

`validateAge` throws when the age is under 18, and prints otherwise:

```java
public static void validateAge(int age) throws InvalidAgeException {
    if (age < MINIMUM_AGE) {
        throw new InvalidAgeException(age, MINIMUM_AGE);
    }
    System.out.println("Age valid message.");
}
```

### Checked or unchecked?

`InvalidAgeException extends Exception`, which makes it **checked**: the compiler
forces every caller to either catch it or declare `throws`. Deleting the try/catch
in `main` is a compile error, not a runtime surprise.

That is the right call here because an invalid age is an *expected* outcome of
validating untrusted input — something the caller can sensibly recover from by
asking again. Had it extended `RuntimeException` it would compile everywhere and
blow up in production. The rule of thumb: **checked for conditions the caller can
reasonably handle, unchecked for bugs.** Both kinds appear in this lab, deliberately
— see the review section.

### The exception carries data, not just a string

```java
public InvalidAgeException(int age, int minimumAge) {
    super("Invalid age: " + age + ". Minimum accepted age is " + minimumAge + ".");
    this.age = age;
    this.minimumAge = minimumAge;
}
```

The message is for a human; the `age` and `minimumAge` fields are for the *code*
that catches it. `App` uses them to compute "short by 3 years" without parsing the
message string. An exception that only carries text forces callers to string-match,
which breaks the moment someone rewords the message.

The threshold is passed in rather than hardcoded, so the policy lives in `App`
(`MINIMUM_AGE`) and the exception stays a plain carrier.

---

## Exercise 2 — the online wallet

`Wallet.withdrawToBank` moves money out of the wallet and into a `BankAccount`:

```java
public void withdrawToBank(BigDecimal amount, BankAccount destination)
        throws InsufficientFundsException {

    if (destination == null)          throw new IllegalArgumentException(...);
    if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException(...);
    if (amount.compareTo(balance) > 0) throw new InsufficientFundsException(amount, balance);

    balance = balance.subtract(amount);
    destination.deposit(amount);
}
```

### Every check happens before any mutation

This ordering is the most important line-level decision in the class. All three
guards run *first*; only then does any balance change. A rejected withdrawal
therefore leaves the wallet **and** the bank account exactly as they were — there is
no partial transfer, and no state to roll back.

Get this backwards — debit first, validate later — and a thrown exception leaves
money deducted from one side and never credited to the other. There is a test
(`failed_withdrawal_leaves_both_balances_untouched`) that pins this down, because it
is the kind of property that silently regresses.

### Money is `BigDecimal`, never `double`

This matters more in a wallet than anywhere else:

```text
double  : 0.10 + 0.20     = 0.30000000000000004
double  : is 0.10+0.20 == 0.30 ? false
BigDec  : 0.10 + 0.20     = 0.30
BigDec  : equals 0.30 ?   = true
```

`double` is binary floating point and cannot represent most decimal fractions
exactly, so balances drift and comparisons like "is the wallet empty?" start
lying. `BigDecimal` is exact.

One trap worth knowing: always construct from a **string**, never a double literal.
`new BigDecimal(0.1)` inherits the error it was trying to avoid:

```text
new BigDecimal(0.1) = 0.1000000000000000055511151231257827021181583404541015625
```

Every amount in this lab is built with `new BigDecimal("500.00")`.

---

## Output

```text
=== Exercise 1 - custom checked exception ===
validateAge(25):
Age valid message.
validateAge(18):
Age valid message.
validateAge(15):
  rejected -> Invalid age: 15. Minimum accepted age is 18.
  short by 3 years

=== Exercise 2 - online wallet ===
  wallet(Ibrahim) = 500.00 | bank(SA-1383-0001) = 1200.00

withdrawToBank(120.50):
  transferred 120.50 to SA-1383-0001
  wallet(Ibrahim) = 379.50 | bank(SA-1383-0001) = 1320.50

withdrawToBank(900.00):
  declined -> Cannot withdraw 900.00: wallet balance is only 379.50 (short by 520.50).
  deposit at least 520.50 to complete this withdrawal
  wallet(Ibrahim) = 379.50 | bank(SA-1383-0001) = 1320.50

withdrawToBank(379.50):
  transferred 379.50 to SA-1383-0001
  wallet(Ibrahim) = 0.00 | bank(SA-1383-0001) = 1700.00
```

Note the middle transfer: the decline leaves both balances identical to the line
above it. The last transfer takes the balance to exactly `0.00`, confirming that
withdrawing the *entire* balance is allowed — only going over it is not.

## Tests

```text
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Test | Pins down |
| --- | --- |
| `under_age_throws_and_carries_the_offending_age` | The exception exposes `age` and `minimumAge` as data |
| `exactly_eighteen_is_accepted` | The boundary: 18 passes |
| `seventeen_is_rejected` | The boundary: 17 fails |
| `successful_withdrawal_moves_money_between_both_sides` | Wallet debited *and* account credited |
| `overdraw_throws_and_reports_the_shortfall` | `getShortfall()` returns the real gap |
| `failed_withdrawal_leaves_both_balances_untouched` | Atomicity — the important one |
| `withdrawing_the_entire_balance_is_allowed` | `amount == balance` is not an overdraw |
| `non_positive_amounts_are_programming_errors_not_business_failures` | Negative, zero and null give `IllegalArgumentException`, not `InsufficientFundsException` |
| `a_wallet_cannot_open_with_a_negative_balance` | Invalid state is rejected at construction |

The two boundary tests exist because `<` vs `<=` is the single most likely bug in
either exercise, and neither the program output nor a casual read would catch it.

---

## Exception-handling quality review

> The handout says to use GitHub Copilot for this review. I am Claude, not Copilot,
> so treat what follows as **a review, not the one the handout names** — if your
> instructor wants Copilot's output specifically, run it yourself and compare. The
> findings below were applied to the code before committing.

**1. Checked and unchecked are used for different jobs, on purpose.**

| Situation | Exception | Why |
| --- | --- | --- |
| Age below 18 | `InvalidAgeException` (checked) | Expected result of validating input; caller can re-prompt |
| Withdrawal exceeds balance | `InsufficientFundsException` (checked) | Normal business outcome; caller can show a message |
| Negative / zero / null amount | `IllegalArgumentException` (unchecked) | A *caller bug*. No sane recovery — fix the call site |

Making the last row checked would force `try`/`catch` around calls that should never
fail, and that noise is exactly what trains people to write empty catch blocks.

**2. No exception is swallowed.** There is no `catch (Exception e) {}` and no bare
`catch (Exception e)` anywhere. Each catch names the specific type it can handle and
does something real with it. Catching broadly would also capture the
`IllegalArgumentException`s above and hide genuine bugs.

**3. Exceptions carry structured data.** Both custom types expose typed accessors
(`getAge`, `getShortfall`, …) so callers branch on fields rather than parsing
`getMessage()`. `getShortfall()` in particular turns "declined" into the actionable
"deposit at least 520.50".

**4. Messages say what failed and with what values.** `Cannot withdraw 900.00:
wallet balance is only 379.50 (short by 520.50)` is a message you can act on from a
log alone.

**5. Validation precedes mutation**, so failures cannot leave a half-completed
transfer. Covered by a test.

**6. `serialVersionUID` is declared** on both exceptions. `Throwable` is
`Serializable`, so without an explicit ID the JVM generates one from the class
structure and it silently changes whenever a field is added.

### Known limits

Worth stating rather than pretending otherwise:

- **Not thread-safe.** Two concurrent `withdrawToBank` calls on one wallet could
  both pass the balance check before either subtracts. Single-threaded here, so no
  locking was added — but in a real wallet this is the first thing to fix.
- **`withdrawToBank` debits then credits.** If `deposit` ever threw, money would
  vanish. It cannot today, because the amount is already validated positive and
  `deposit` rejects only non-positive amounts. A real system would need a
  transaction or a compensating action rather than relying on that.
- **No currency type.** `BigDecimal` fixes the arithmetic but nothing stops adding
  SAR to USD. A `Money` type carrying an amount *and* a currency would be the next
  step.
