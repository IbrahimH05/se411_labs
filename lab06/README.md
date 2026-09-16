# Lab 06 — Logging with SLF4J

SE411, Fall 2026/2027. Replacing `System.out.println` with a real logging facade,
swapping the concrete backend underneath it, and configuring log4j to write
levelled, formatted output to a file.

## Files

| File | What it is |
| --- | --- |
| [`pom.xml`](pom.xml) | Starter POM plus `slf4j-api` and the `slf4j-log4j12` binding |
| [`src/main/resources/log4j.properties`](src/main/resources/log4j.properties) | Level, appender, output file and message format |
| [`App.java`](src/main/java/edu/spu/se411/lab06_logging/App.java) | Entry point — `info` on start/end, `error` on caught exceptions |
| [`model/WalletAccount.java`](src/main/java/edu/spu/se411/lab06_logging/model/WalletAccount.java) | `debug` on creation, deposit and withdraw |
| [`exceptions/InsufficientFundsException.java`](src/main/java/edu/spu/se411/lab06_logging/exceptions/InsufficientFundsException.java) | `warn` when the exception is constructed |

Package is `edu.spu.se411.lab06_logging` — note `spu`, not `psu`. That is how the
starter code ships and what the POM's `<mainClass>` points at, so it was left alone
rather than silently "corrected".

## Running it

```bash
mvn clean package exec:java
```

Output goes to `./logs/App/log4j/log.out`, **not** the console — see below. Tested
with Maven 3.9.16 and OpenJDK 26.0.2.1.

```bash
cat logs/App/log4j/log.out
```

`logs/` is gitignored; it is regenerated on every run.

---

## What SLF4J actually is

SLF4J is a **façade**, not a logging library. `slf4j-api` gives you `Logger` and
`LoggerFactory` and nothing else — no ability to write a single line anywhere. A
separate *binding* jar supplies the implementation, and it is chosen at
**deploy time by what is on the classpath**, not in your code. That is the whole
point: application code calls `logger.info(...)` and never learns which backend
serves it.

The lab proves this by running the same unchanged code against two different
backends.

### Stage 1 — `slf4j-api` alone

```text
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
```

Exactly the warning the handout predicts. The important detail is what it does
*next*: it falls back to a **no-op logger** and the program runs fine.
`logger.info("Application is starting...")` executed and produced nothing at all.
Missing logging config silently discards your logs rather than crashing — which is
why "the logs are empty" is a real production failure mode.

### Stage 2 — add logback

```text
13:20:20.017 [...App.main()] INFO edu.spu.se411.lab06_logging.App -- Application is starting...
```

One dependency, no configuration file, and it works — logback is SLF4J's reference
implementation and ships a sensible console default. No Java source changed between
stage 1 and stage 2.

### Stage 3 — swap logback for log4j

Removing logback and adding `slf4j-log4j12` swaps the engine underneath the same
unchanged calls. This is the lesson worth keeping: **the binding is a deployment
decision, not a code decision.**

> ⚠️ Only ever put **one** binding on the classpath. Two produce a
> `Class path contains multiple SLF4J providers` warning and SLF4J picks one
> arbitrarily. That is why logback had to be *removed*, not just added to.

---

## The log4j configuration

`log4j.properties` must sit in `src/main/resources` so Maven copies it onto the
classpath, where log4j auto-discovers it by name.

```properties
log4j.rootLogger = ERROR, LOG1
log4j.appender.LOG1=org.apache.log4j.FileAppender
log = ./logs/App/log4j
log4j.appender.LOG1.File=${log}/log.out
log4j.appender.LOG1.layout=org.apache.log4j.PatternLayout
log4j.appender.LOG1.layout.conversionPattern=%d %p [%t] %C - %m%n
```

| Piece | Meaning |
| --- | --- |
| `rootLogger = LEVEL, LOG1` | Minimum level to keep, then the appender(s) to send it to |
| `FileAppender` | Write to a file. **There is no console appender**, which is why `mvn exec:java` prints nothing |
| `log = ./logs/App/log4j` | A user-defined variable, referenced as `${log}` |
| `PatternLayout` | Formats each record |

Pattern tokens: `%d` timestamp, `%p` priority/level, `%t` thread, `%C` class,
`%m` message, `%n` newline.

**Levels are a threshold, not a filter.** `ERROR` keeps ERROR and above; `DEBUG`
keeps everything. Ordering is `TRACE < DEBUG < INFO < WARN < ERROR < FATAL`.

## Output at each level

The handout says that after setting `ERROR` you should see *no* messages. That is
true **at the point in the handout where it is said** — the only statement that
exists then is `logger.info(...)`, and `INFO < ERROR`, so it is dropped. Once the
required `error`-level statements are added, `ERROR` does produce output. Both
behaviours below are from real runs.

### `log4j.rootLogger = INFO, LOG1`

```text
2026-09-16 13:22:12,421 INFO [main] App - Application is starting...
2026-09-16 13:22:12,422 WARN [main] exceptions.InsufficientFundsException - InsufficientFundsException created: Insufficient funds. Your balance is 1000.0
2026-09-16 13:22:12,423 ERROR [main] App - Withdrawal failed for amount 1500
2026-09-16 13:22:12,424 ERROR [main] App - Deposit failed for amount -100
2026-09-16 13:22:12,424 INFO [main] App - Application is ending...
```

The `debug` statements are gone — wallet creation, deposits and withdrawals are all
below the threshold. Stack traces are in the file too; they are omitted here for
readability.

### `log4j.rootLogger = DEBUG, LOG1`

```text
2026-09-16 13:24:59,522 INFO [main] App - Application is starting...
2026-09-16 13:24:59,523 DEBUG [main] model.WalletAccount - Wallet account created with balance 1000.0
2026-09-16 13:24:59,523 DEBUG [main] model.WalletAccount - Withdraw requested: amount=1500.0, balance=1000.0
2026-09-16 13:24:59,524 WARN [main] exceptions.InsufficientFundsException - InsufficientFundsException created: Insufficient funds. Your balance is 1000.0
2026-09-16 13:24:59,524 ERROR [main] App - Withdrawal failed for amount 1500
2026-09-16 13:24:59,525 DEBUG [main] model.WalletAccount - Deposit requested: amount=-100.0, balance=1000.0
2026-09-16 13:24:59,525 ERROR [main] App - Deposit failed for amount -100
2026-09-16 13:24:59,525 DEBUG [main] model.WalletAccount - Deposit requested: amount=250.0, balance=1000.0
2026-09-16 13:24:59,525 DEBUG [main] model.WalletAccount - Deposit successful. New balance: 1250.0
2026-09-16 13:24:59,525 DEBUG [main] model.WalletAccount - Withdraw requested: amount=400.0, balance=1250.0
2026-09-16 13:24:59,525 DEBUG [main] model.WalletAccount - Withdrawal successful. Remaining balance: 850.0
2026-09-16 13:24:59,525 INFO [main] App - Application is ending...
```

Counts by level at `DEBUG`: 7 DEBUG, 2 INFO, 1 WARN, 2 ERROR.

The committed `log4j.properties` is set to `DEBUG` so every required statement is
visible. Change the one word on the first line to see the threshold move.

## Required logging events

Every row of the handout's table, and where it lives:

| Event | Level | Where |
| --- | --- | --- |
| Application starts / ends | `info` | `App.main` |
| Wallet account created | `debug` | `WalletAccount` constructor |
| Deposit, withdraw | `debug` | `WalletAccount.deposit` / `.withdraw` |
| `InsufficientFundsException` object created | `warn` | The exception's constructor |
| Thrown exception | `error` | The `catch` blocks in `App.main` |

---

## Finding: `slf4j-log4j12` is not what you get

The handout's coordinates resolve, but not to log4j. `slf4j-log4j12:2.0.16` is a
**Maven relocation** — its POM contains:

```xml
<distributionManagement>
  <relocation>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-reload4j</artifactId>
    <version>2.0.16</version>
  </relocation>
</distributionManagement>
```

The jars that actually land on the classpath:

```text
slf4j-api-2.0.16.jar
slf4j-reload4j-2.0.16.jar
reload4j-1.2.22.jar
```

**reload4j**, not log4j. This is a good outcome: log4j 1.2.17 has been end-of-life
since 2015 and carries CVE-2019-17571 (deserialization RCE in `SocketServer`) and
CVE-2021-4104 (untrusted deserialization via `JMSAppender`). reload4j is a
maintained fork that patches them while keeping the *same* `org.apache.log4j`
package names — which is exactly why `log4j.properties` still works unchanged and
why the swap is invisible.

Worth knowing for the exam: log4j **1.x** is a different project from Log4j **2.x**,
and neither is affected by Log4Shell in the way the other is — Log4Shell
(CVE-2021-44228) was a Log4j 2.x JNDI lookup bug. Nothing here is exposed to it.

---

## AI evaluation of the logging strategy

The handout's last step is to have an AI evaluate the logging strategy and apply its
recommendations. I am that AI, so this section is that review — and unlike Lab 05's
Copilot step, it is genuinely the tool the task asks for. Findings are split into
what was **applied** and what is **documented but deliberately not applied**,
because some recommendations contradict the configuration the handout pins.

### Applied

**1. Parameterized messages, never string concatenation.**

```java
logger.debug("Withdraw requested: amount={}, balance={}", amount, balance);   // this
logger.debug("Withdraw requested: amount=" + amount);                          // not this
```

With `{}` placeholders the message is only assembled if the level is enabled. With
concatenation the string is built on *every* call — including the thousands of
DEBUG calls in a production run where DEBUG is off. It is the single highest-value
logging habit.

**2. The exception goes in as the last argument, not into the message.**

```java
logger.error("Withdrawal failed for amount {}", 1500, e);
```

SLF4J treats a trailing `Throwable` specially: it is *not* consumed by a `{}`
placeholder, and the backend prints the full stack trace. `logger.error("... " +
e.getMessage())` throws the stack trace away, which is the part you actually need at
3am. The stack traces in the log file above come from this.

**3. `System.out.println` removed entirely.** The starter printed results to stdout
from inside `WalletAccount`. Those bypass levels, formatting, appenders and the log
file — invisible to anything that reads logs. All replaced with `debug`. There is
now no `System.out` anywhere in the project.

**4. `private static final` on every logger.** The handout's snippet writes
`static Logger logger = ...`, which is package-visible and reassignable. All three
classes now use `private static final`: one instance per class, no accidental
reassignment, no leaking out of the class.

### Documented, not applied

These would improve the code but contradict what the handout specifies, so the
committed version follows the handout and records the reasoning here.

**5. `%C` in the pattern is slow.** Recovering the calling *class* forces log4j to
build and walk a throwable stack trace on every record; log4j's own docs call this
extremely slow. `%c` (the logger name) is nearly free and, since every logger here is
named after its class, prints the same text:

```properties
log4j.appender.LOG1.layout.conversionPattern=%d %p [%t] %c - %m%n
```

**6. `FileAppender` never rotates.** It appends forever — one unbounded file that
eventually fills the disk. A production config wants rotation:

```properties
log4j.appender.LOG1=org.apache.log4j.RollingFileAppender
log4j.appender.LOG1.MaxFileSize=10MB
log4j.appender.LOG1.MaxBackupIndex=5
```

**7. No console output at all.** Handy in production, awkward in a lab — `mvn
exec:java` looks like it did nothing. Adding a second appender costs one line and
does not disturb `LOG1`:

```properties
log4j.rootLogger = DEBUG, LOG1, CONSOLE
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.conversionPattern=%d %p %c - %m%n
```

**8. Logging inside an exception constructor is an unusual coupling.** The handout
requires `warn` when `InsufficientFundsException` is created, so it is implemented —
but it means a domain exception now depends on a logging framework, and it logs even
when the caller catches and handles the exception harmlessly. Constructing an
exception is not by itself a noteworthy event; *failing to handle* one is.

**9. That same requirement produces double logging.** One failed withdrawal writes
two records — `warn` from the constructor and `error` from the catch block. This is
the classic *log-and-throw* anti-pattern: the rule is normally log **or** propagate,
not both, and the right place is wherever the exception is finally handled, which is
the only place that knows whether it mattered. Both are kept here because the
handout's table mandates both levels. It is visible in the `DEBUG` output above —
the WARN and the ERROR immediately following it describe one event.

**10. Balances are written to the log.** `balance=1000.0` is fine in a lab and a
compliance problem in a real wallet, where logs are retained, shipped to third-party
aggregators and read by staff who are not entitled to customer financial data. Worth
carrying into the FinTech main project: decide early what must never reach a log line.

### Not done

No unit tests were added. Asserting on log output needs a capturing appender, which
is more scaffolding than this lab justifies — the evidence here is the captured log
files at three different levels instead.
