# Lab 02 — Introduction to Java Generics

SE411, Fall 2026/2027. Four exercises on generic classes, bounded type
parameters, a type-changing pipeline, and wildcards.

## Files

| File | Exercise | What it shows |
| --- | --- | --- |
| [`PrintableList.java`](src/lab02/PrintableList.java) | 1 | A generic class `PrintableList<T>` backed by a `List<T>`, built from an array |
| [`NumberBox.java`](src/lab02/NumberBox.java) | 2 | A bounded type parameter, `T extends Number`, plus addition and summing |
| [`Transformer.java`](src/lab02/Transformer.java) | 3 | `Transformer<T, R>` — one transformation step, generic over input *and* output |
| [`Pipeline.java`](src/lab02/Pipeline.java) | 3 | `Pipeline<T, R>` — an immutable chain whose output type changes as steps are added |
| [`Main.java`](src/lab02/Main.java) | all + 4 | Drives every exercise; `printList` / `sumNumbers` wildcard methods live here |

## Running it

From this directory:

```bash
javac -d out $(find src -name '*.java') && java -cp out lab02.Main
```

Compiled and run against **javac 26.0.2.1**; clean under `javac -Xlint:all`
(no warnings, including no unchecked warnings escaping `Pipeline`).

## Notes on each exercise

### Exercise 1 — `PrintableList<T>`

The constructor takes a `T[]` and stores it via `Arrays.asList`. One wrinkle worth
knowing: `Arrays.asList` returns a *fixed-size view* over the array, not a copy —
calling `add` or `remove` on it throws `UnsupportedOperationException`, and writing
through it mutates the original array. It's copied into an `ArrayList` so the field
behaves like an ordinary list.

### Exercise 2 — `NumberBox<T extends Number>`

The bound is what earns its keep here. With a plain `<T>` the compiler only knows
`T` is an `Object`, so `item.doubleValue()` wouldn't compile. `T extends Number`
guarantees the numeric API, at the cost of making `NumberBox<String>` illegal —
verified, that really is a compile error:

```
error: type argument String is not within bounds of type-variable T
```

`add` takes a `Number` rather than a `T`, so a `NumberBox<Double>` can be added to
an `int`. `sum` takes `List<? extends Number>`, which is what lets one method accept
`List<Integer>`, `List<Double>` and `List<Long>` alike — see Exercise 4.

### Exercise 3 — `Pipeline<T, R>`

The two type parameters do different jobs, and that's the whole idea:

- **`T`** — the type the pipeline is fed. Fixed at creation, never changes.
- **`R`** — the type it currently produces. Changes with every step appended.

A type parameter can't be reassigned, so `add` *cannot* mutate `R` in place.
Instead it returns a brand-new `Pipeline<T, V>`, and chaining `add` calls walks
the output type forward at compile time:

```java
Pipeline.start(String.class)   // Pipeline<String, String>
        .add(String::trim)     // Pipeline<String, String>   type-preserving
        .add(String::length)   // Pipeline<String, Integer>  type-changing
        .add(n -> n * 2.5)     // Pipeline<String, Double>
        .execute("  hi  ");    // returns Double
```

Two details:

- **`add` takes `Transformer<? super R, ? extends V>`**, not `Transformer<R, V>`.
  PECS: the step *consumes* `R`, so anything handling `R` or a supertype will do.
  This is why a `Transformer<Object, String>` is a legal step on a pipeline of
  `Integer` — verified to compile.
- **The steps are stored as `List<Transformer<?, ?>>`** and `execute` casts to
  `Transformer<Object, Object>`. The intermediate types between steps aren't
  expressible as one type argument and are erased at runtime anyway. The cast is
  safe because `add` is the only way to build the list, and its signature already
  proved at compile time that step *n*'s output feeds step *n+1*'s input. Appending
  a `Transformer<String, ?>` to a pipeline producing `Integer` genuinely fails:

  ```
  error: incompatible types: cannot infer type-variable(s) V
      (argument mismatch; Transformer<String,Integer> cannot be
       converted to Transformer<? super Integer,? extends Integer>)
  ```

Because pipelines are immutable, an earlier one can be branched into a different
output type without disturbing the original — `Main` does exactly that.

> The handout writes both `PipeLine<T, R>` and `Pipeline<T>`. The class is named
> `Pipeline` with the two parameters, since "return a new pipeline with the updated
> output type" is only expressible if the output type is its own parameter.

### Exercise 4 — wildcards

Both methods are in `Main`:

- **`printList(List<?>)`** — the unbounded wildcard means "a list of *some* unknown
  type". Enough to read elements out as `Object`; nothing can be added, because the
  compiler can't know which type would be legal. Even `l.add("x")` on a
  `List<?>` that really is a `List<String>` is rejected (`String cannot be converted
  to CAP#1`).
- **`sumNumbers(List<? extends Number>)`** — the upper bound is what permits
  `doubleValue()` on the elements.

The reason the wildcard is needed at all is **invariance**: `List<Integer>` is *not*
a subtype of `List<Number>`, so a `List<Number>` parameter would have rejected every
call in the output below.

## Program output

Actual output, not transcribed by hand:

```text

=== Exercise 1 — PrintableList<T> ===
PrintableList<String>, 3 items:
SE411
SE311
CS201

PrintableList<Integer>, 3 items:
2024
2025
2026

=== Exercise 2 — NumberBox<T extends Number> ===
intBox           = NumberBox(10)
intBox.getItem() = 10
intBox.add(5)    = 15.0
after setItem(42), getItem() = 42

doubleBox            = NumberBox(2.5)
doubleBox.add(0.75)  = 3.25
doubleBox.add(3)     = 5.5

NumberBox.sum([1, 2, 3, 4, 5]) = 15.0
NumberBox.sum([1.5, 2.5, 3.0]) = 7.0

=== Exercise 3 — Pipeline<T, R> ===
input   = "   generics are structural   "
trim            (String)   -> "generics are structural"
+ upper         (String)   -> "GENERICS ARE STRUCTURAL"
+ length       (Integer)   -> 23
+ times 2.5     (Double)   -> 57.5
steps in the last pipeline = 4

branching off the length pipeline instead:
+ length > 20  (Boolean)   -> true
original 4-step pipeline is unchanged, size = 4

describe.execute(7) = "square = 49"

=== Exercise 4 — wildcards ===
printList(List<String>):
  wildcard
  bounded
  erasure
printList(List<Integer>):
  4
  8
  15
  16
  23
  42

sumNumbers([4, 8, 15, 16, 23, 42])  = 108.0
sumNumbers([0.5, 1.25, 2.75]) = 4.5
sumNumbers(List<Long>)          = 600.0
```
