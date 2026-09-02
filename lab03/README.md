# Lab 03 — JUnit Intro

SE411, Fall 2026/2027. Writing unit tests for the provided `Stack<E>` class using
JUnit 5 (Jupiter) under Maven.

## Files

| File | What it is |
| --- | --- |
| [`pom.xml`](pom.xml) | The instructor's starter POM, **unmodified** |
| [`src/main/java/edu/psu/se411/model/Stack.java`](src/main/java/edu/psu/se411/model/Stack.java) | Starter code — the class under test |
| [`src/main/java/edu/psu/se411/App.java`](src/main/java/edu/psu/se411/App.java) | Starter code — empty `main` |
| [`src/test/java/edu/psu/se411/model/StackTest.java`](src/test/java/edu/psu/se411/model/StackTest.java) | **The work for this lab** — 6 test cases |

The test class sits in `src/test/java` under package `edu.psu.se411.model` — the
same package as `Stack`, as the handout requires. That isn't cosmetic: same-package
tests can reach package-private members, and Maven keeps `src/main/java` and
`src/test/java` as separate compilation roots so tests never ship in the jar.

## Running it

```bash
mvn test
```

Eclipse equivalent: right-click `pom.xml` → Run As → Maven test.

Tested with Maven 3.9.16 and OpenJDK 26.0.2.1.

## The class under test

`Stack<E>` wraps an `ArrayList<E>` and exposes only two operations:

- `push(E)` — appends to the end of the list
- `pop()` — removes and returns the **last** element, or throws
  `NoSuchElementException("Stack is empty, cannot pop")` when empty

Last-in-first-out, so the end of the list is the top of the stack. The
`Stack(int capacity)` constructor silently falls back to 10 for a non-positive
capacity, which is worth a test of its own.

## The test cases

Each test gets a fresh stack from a `@BeforeEach` method, so no test can be
affected by another's leftovers.

| Test | Handout step | What it pins down |
| --- | --- | --- |
| `push_push_pop_returns_last_pushed` | 5 | `push("Z")`, `push("A")` → `pop()` must give `"A"`, not `"Z"` — the core LIFO guarantee |
| `pop_empty_stack` | 8 | Popping an empty stack throws `NoSuchElementException`, and the message matches |
| `elements_pop_in_reverse_order` | 9 | Three pushes come back in exactly reverse order |
| `pop_removes_the_element_so_stack_empties` | extra | `pop` actually *removes*; a second `pop` throws |
| `stack_is_generic_and_works_with_integers` | extra | The generic parameter works for `Integer`, not just `String` |
| `non_positive_capacity_falls_back_to_default` | extra | `new Stack<>(0)` is still usable |

### Why `import static`

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
```

A static import pulls in the static *members* of a class, so you write
`assertEquals(...)` instead of `Assertions.assertEquals(...)`. In a test file where
nearly every line is an assertion, that's the difference between readable and noisy.
The imports here are named individually rather than `.*`, which keeps it obvious
where each assertion comes from.

### Why `assertThrows` instead of try/catch

```java
NoSuchElementException thrown = assertThrows(
        NoSuchElementException.class,
        () -> stringStack.pop(),
        "Expected pop from empty Stack to throw, but it didn't");
```

The lambda defers the call so JUnit can invoke it and catch the result. A
try/catch version needs a `fail()` after the call to catch the case where nothing
throws — easy to forget, and a silent false pass when you do. `assertThrows` also
*returns* the exception, so the message can then be asserted.

## Results

All 6 tests pass:

```text
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running edu.psu.se411.model.StackTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s -- in edu.psu.se411.model.StackTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.580 s
[INFO] Finished at: 2026-09-02T13:08:38+03:00
[INFO] ------------------------------------------------------------------------
```

### Step 7 — forcing a failure

The handout asks you to break the test on purpose to confirm the report is real.
Changing the assertion to `assertEquals("Z", stringStack.pop())` gives:

```text
[ERROR] Tests run: 6, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
[ERROR] edu.psu.se411.model.StackTest.push_push_pop_returns_last_pushed <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <Z> but was: <A>
[ERROR]   StackTest.push_push_pop_returns_last_pushed:27 expected: <Z> but was: <A>
[INFO] BUILD FAILURE
```

Two things to notice: the build **fails** rather than just warning — so a broken
test blocks a Maven build — and the report names the file and line. The committed
test is the passing version.

## Two findings

**1. The starter POM needs no changes.** It declares only `junit-jupiter-api` and
`junit-jupiter-params`, with no `junit-jupiter-engine` and no explicit
`maven-surefire-plugin`. On older Maven that combination doesn't run anything:
Surefire needs a test *engine* at runtime, and Maven's old default Surefire (2.12.4)
predates JUnit 5 entirely. On Maven 3.9.16 the default Surefire is 3.x, which
resolves the Jupiter engine itself — visible in the build log downloading
`junit-jupiter-engine-5.11.0.jar` even though the POM never asks for it. So the POM
is committed exactly as provided. If tests appear not to run on an older Maven, add
`junit-jupiter-engine` with test scope.

**2. The handout's expected exception message is wrong.** It asserts:

```java
assertTrue(thrown.getMessage().equals("Stack is empty,can’t pop"));
```

but `Stack.pop()` throws `"Stack is empty, cannot pop"`. Two differences — `can’t`
(with a curly apostrophe) vs `cannot`, and the missing space after the comma. Copied
verbatim, that test fails:

```text
[ERROR] StackTest.pop_empty_stack:38 expected: <true> but was: <false>
```

The committed test asserts the message the class actually throws.

This is also a good argument for not asserting on exact message strings: the
exception *type* is the contract, the wording is an implementation detail that a
refactor can change. `assertThrows(NoSuchElementException.class, ...)` alone would
have been immune. The message assertion is kept because the handout asks for it.
