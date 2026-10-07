# Lab 09 — Managing an Inventory with the Strategy Pattern

SE411, Fall 2026/2027. A generic `Inventory<T>`, pluggable search behaviour, and a
display method that works for any element type.

## Files

| File | Part | What it is |
| --- | --- | --- |
| [`Inventory.java`](src/main/java/edu/psu/se411/lab09_inventory/Inventory.java) | 1 + 2 | Generic container; `add`, `remove`, `getItems`, `findItems` |
| [`model/Item.java`](src/main/java/edu/psu/se411/lab09_inventory/model/Item.java) | 1 | Abstract base — id and name, shared by both product types |
| [`model/Book.java`](src/main/java/edu/psu/se411/lab09_inventory/model/Book.java) | 1 | id, name, author |
| [`model/ElectronicDevice.java`](src/main/java/edu/psu/se411/lab09_inventory/model/ElectronicDevice.java) | 1 | id, name, category |
| [`search/SearchStrategy.java`](src/main/java/edu/psu/se411/lab09_inventory/search/SearchStrategy.java) | 2 | The strategy interface, as the handout specifies |
| [`search/TypedSearchStrategy.java`](src/main/java/edu/psu/se411/lab09_inventory/search/TypedSearchStrategy.java) | 2 | **Reusable base** for strategies that target one subtype |
| [`search/SearchById.java`](src/main/java/edu/psu/se411/lab09_inventory/search/SearchById.java) | 2 | Any `Item`, by id |
| [`search/SearchByName.java`](src/main/java/edu/psu/se411/lab09_inventory/search/SearchByName.java) | 2 | Any `Item`, by name fragment |
| [`search/SearchByAuthor.java`](src/main/java/edu/psu/se411/lab09_inventory/search/SearchByAuthor.java) | 2 | `Book` only |
| [`search/SearchByCategory.java`](src/main/java/edu/psu/se411/lab09_inventory/search/SearchByCategory.java) | 2 | `ElectronicDevice` only |
| [`search/AllOf.java`](src/main/java/edu/psu/se411/lab09_inventory/search/AllOf.java) | 2 | Combines strategies with AND |
| [`util/Validate.java`](src/main/java/edu/psu/se411/lab09_inventory/util/Validate.java) | — | Shared text validation |
| [`App.java`](src/main/java/edu/psu/se411/lab09_inventory/App.java) | 1–3 | Main program, `displayInventory` |
| [`InventoryTest.java`](src/test/java/edu/psu/se411/lab09_inventory/InventoryTest.java) | 1–3 | 22 tests |

The starter's `pom.xml`, `App` skeleton and `log4j.properties` are kept; JUnit was
added to the POM for the tests.

## Running it

```bash
mvn clean package exec:java
```

```bash
cat logs/log.out
```

Tested with Maven 3.9.16 and OpenJDK 26.0.2.1. `logs/` is gitignored.

---

## Part 1 — the generic container

```java
public class Inventory<T> {
    private final List<T> items = new ArrayList<>();

    public void add(T item)
    public boolean remove(T item)
    public List<T> getItems()
}
```

`T` is unbounded, so an `Inventory` can hold anything — the demo puts `String`s in one
to prove it. Type safety comes from the declaration site: an `Inventory<Item>` accepts
books and devices, while an `Inventory<ElectronicDevice>` rejects a `Book` at compile
time:

```text
error: incompatible types: Book cannot be converted to ElectronicDevice
```

`getItems()` returns an unmodifiable view. Handing out the live list would let a
caller bypass `add` and its null check, and mutate the inventory without the owner
knowing.

### Why `Item` exists

The handout asks for an `Inventory<Item>` holding books *and* devices, which only
works if both share a supertype. `Item` is an abstract class rather than an interface
because the two types share **state** — every product has an id and a name — and an
interface cannot hold fields. Validation and `equals`/`hashCode` are written once
there.

`equals` compares ids only: two records of the same product are the same product
regardless of how the title was typed. That is what makes `remove` work on a
freshly-constructed instance.

---

## Part 2 — the Strategy pattern

```java
@FunctionalInterface
public interface SearchStrategy<T> {
    boolean matches(T item);
}
```

Each search behaviour becomes its own class. The alternative is `Inventory` growing
`findByName`, `findById`, `findByAuthor`, `findByCategory`… — a container that has to
be edited every time someone invents a new query, and that has to know about `Book`
and `ElectronicDevice`. With Strategy, `Inventory` knows only `matches`.

Because the interface has a single method it is also a functional interface, so a
one-off search needs no class at all:

```java
inventory.findItems(item -> item.getId().startsWith("B-"));
```

### The signature decision

The obvious version is `findItems(SearchStrategy<T> strategy)`. The version here is:

```java
public List<T> findItems(SearchStrategy<? super T> strategy)
```

**PECS** — a strategy only *consumes* items, so it is a Consumer, and consumers take
`? super T`. The payoff is reuse: a strategy written against `Item` can search an
`Inventory<Book>`, because `Item` is a supertype of `Book`. Verified both ways:

```java
SearchStrategy<Item> byGoetz = new SearchByAuthor("Goetz");

inventory.findItems(byGoetz);   // Inventory<Item> — compiles
booksOnly.findItems(byGoetz);   // Inventory<Book> — compiles, same object
```

With a plain `SearchStrategy<T>`, the second line would not compile and every
narrower inventory would need its own strategy classes.

### The problem `TypedSearchStrategy` solves

`SearchByAuthor` only makes sense for a `Book`, but it has to be usable against an
`Inventory<Item>`. Declaring it `SearchStrategy<Book>` does **not** work — generics
are invariant, and `? super Item` does not admit `SearchStrategy<Book>`:

```text
error: incompatible types: SearchStrategy<Book> cannot be converted to SearchStrategy<? super Item>
```

So the typed strategies are `SearchStrategy<Item>` that narrow internally. The
narrowing is written once in a base class:

```java
public abstract class TypedSearchStrategy<I extends Item> implements SearchStrategy<Item> {
    private final Class<I> type;

    @Override
    public final boolean matches(Item item) {
        return type.isInstance(item) && matchesTyped(type.cast(item));
    }

    protected abstract boolean matchesTyped(I item);
}
```

`SearchByAuthor extends TypedSearchStrategy<Book>` then implements only
`matchesTyped(Book)`. Items of any other type simply do not match — searching a mixed
inventory by author returns the books by that author and ignores the laptops, which
is the sensible answer. `matches` is `final` so a subclass cannot skip the type check.

This is the same template-method trick as Lab 08's `SensorObserver`: the base fixes
the sequence, the subclass fills in the one varying step.

### Strategies compose

`AllOf` takes several strategies and requires all of them to match, so a new query
needs no new class:

```java
new AllOf<Item>(new SearchByAuthor("Bloch"), new SearchByName("java"))
```

That combination — books by Bloch whose title contains "java" — would otherwise be a
fifth strategy class, and the sixth combination a sixth class.

---

## Part 3 — displaying any inventory

```java
public static void displayInventory(Inventory<?> inventory) {
    for (Object item : inventory.getItems()) {
        System.out.println("  " + item);
    }
}
```

The unbounded wildcard means "an `Inventory` of *some* unknown type". That is enough
to read elements out as `Object` and let `toString()` do the work polymorphically —
`Item` overrides it, so books print their author and devices print their category
without `displayInventory` knowing either type exists.

What the wildcard costs is writing: nothing can be added to an `Inventory<?>`, because
the compiler cannot know which type would be legal.

```text
error: incompatible types: Book cannot be converted to CAP#1
  where CAP#1 is a fresh type-variable: CAP#1 extends Object from capture of ?
```

That restriction is the reason the method is safe to hand any inventory at all,
including an `Inventory<String>` — which the demo does.

> `Inventory<? extends Item>` would have been the alternative: it would allow calling
> `getId()` and `getName()` on the elements, at the cost of no longer accepting an
> `Inventory<String>`. The handout asks for `Inventory<?>`, and relying on `toString()`
> is what makes it work for literally any type.

---

## Program output

```text

== Inventory contents (6 items) ==
  B-001  Book           Concurrency in Java              by Brian Goetz
  B-002  Book           Effective Java                   by Joshua Bloch
  B-003  Book           Java Puzzlers                    by Joshua Bloch
  E-001  Electronics    ThinkPad X1 Carbon               category Laptop
  E-002  Electronics    Dell UltraSharp 27               category Monitor
  E-003  Electronics    MacBook Air M3                   category Laptop

== Searching with strategies ==

  by name  "Concurrency in Java" -> 1 match(es)
      B-001  Book           Concurrency in Java              by Brian Goetz

  by name  "java" -> 3 match(es)
      B-001  Book           Concurrency in Java              by Brian Goetz
      B-002  Book           Effective Java                   by Joshua Bloch
      B-003  Book           Java Puzzlers                    by Joshua Bloch

  by id    "E-002" -> 1 match(es)
      E-002  Electronics    Dell UltraSharp 27               category Monitor

  by author "Joshua Bloch" -> 2 match(es)
      B-002  Book           Effective Java                   by Joshua Bloch
      B-003  Book           Java Puzzlers                    by Joshua Bloch

  by category "Laptop" -> 2 match(es)
      E-001  Electronics    ThinkPad X1 Carbon               category Laptop
      E-003  Electronics    MacBook Air M3                   category Laptop

  books by Bloch whose name contains "java" -> 2 match(es)
      B-002  Book           Effective Java                   by Joshua Bloch
      B-003  Book           Java Puzzlers                    by Joshua Bloch

== The same strategy object against a narrower inventory ==
  Inventory<Item> : 1 match(es)
  Inventory<Book> : 1 match(es)   (same strategy object)
  findItems(SearchStrategy<? super T>) is what allows both

== displayInventory(Inventory<?>) on a completely unrelated type ==

== Inventory contents (2 items) ==
  USB-C cable
  Laptop charger
```

## Log file

```text
2026-10-07 13:15:55,689 INFO [main] App - Application is starting...
2026-10-07 13:15:55,690 INFO [main] App - Inventory built with 6 items
2026-10-07 13:15:55,692 INFO [main] App - Search [by name  "Concurrency in Java"] matched 1 item(s)
2026-10-07 13:15:55,692 INFO [main] App - Search [by name  "java"] matched 3 item(s)
2026-10-07 13:15:55,693 INFO [main] App - Search [by id    "E-002"] matched 1 item(s)
2026-10-07 13:15:55,693 INFO [main] App - Search [by author "Joshua Bloch"] matched 2 item(s)
2026-10-07 13:15:55,693 INFO [main] App - Search [by category "Laptop"] matched 2 item(s)
2026-10-07 13:15:55,694 INFO [main] App - Search [books by Bloch whose name contains "java"] matched 2 item(s)
2026-10-07 13:15:55,694 INFO [main] App - Application is closing...
```

---

## Tests

```text
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Group | Tests |
| --- | --- |
| Part 1 — container | Add and count; remove; removing something absent returns false; null rejected; returned list unmodifiable; one inventory holds both product types; items identified by id; blank fields rejected |
| Part 2 — searching | The handout's exact `SearchByName("Concurrency in Java")` call; case-insensitive fragment matching; by id; by author; by category; empty result for no match; null strategy rejected |
| Part 2 — type safety | A `Book` strategy never matches a device and never throws; a device strategy never matches a book; **one `SearchStrategy<Item>` object searches both an `Inventory<Item>` and an `Inventory<Book>`**; a strategy written as a lambda |
| Part 2 — composition | `AllOf` requires every strategy to match |
| Part 3 — display | Accepts `Inventory<Item>`, `Inventory<String>` and `Inventory<Integer>`; stdout is captured and asserted to contain each item's details |

The cross-type search test is the one that matters: it fails to **compile** if
`findItems` is narrowed to `SearchStrategy<T>`, so it pins the signature decision in
place rather than leaving it to a comment.

### Compile-time behaviour, checked rather than claimed

The generics claims above were verified by compiling them. Three fail as they should:

| Attempt | Result |
| --- | --- |
| Pass a `SearchStrategy<Book>` to an `Inventory<Item>` | `cannot be converted to SearchStrategy<? super Item>` |
| `add` to an `Inventory<?>` | `Book cannot be converted to CAP#1` |
| `add` a `Book` to an `Inventory<ElectronicDevice>` | `Book cannot be converted to ElectronicDevice` |

and three compile, as they should: `SearchByAuthor` against an `Inventory<Item>`,
the same object against an `Inventory<Book>`, and reading an `Inventory<?>` as
`Object`.

---

## Notes

**1. The handout names the method twice, differently.** Part 2 says to create
`findItem(SearchStrategy<T> startegy)` — singular, and with `startegy` misspelled —
then refers to "the findItems method" and shows `inventory.findItems(...)`. The method
is `findItems`, matching the example call, since that is the one a grader will run.

**2. Search by name is a case-insensitive fragment match.** The handout's example
passes a full title, which a fragment match handles. Making it a substring search also
makes `SearchByName("java")` useful, and it is the behaviour a user expects from a
search box. `SearchById` is an exact match, because an id either is or is not the one
you asked for.

**3. `TypedSearchStrategy` is not in the handout.** The handout asks for strategies
"for Book (e.g., search by author)" without saying how a `Book` strategy reaches an
`Inventory<Item>`. Writing `SearchStrategy<Book>` does not compile against
`findItems`, so some narrowing is unavoidable; doing it once in a base class beats
repeating `instanceof` in every subtype strategy.

**4. `Validate` exists because two packages needed the same check.** The text
validation started as a `protected static` helper on `Item`, which the `search`
package cannot reach — it is neither a subclass nor in the same package. Rather than
widening `Item`'s API or duplicating the check, it moved to a small utility both use.

**5. The starter POM targets `release 24`.** It was left alone; the project compiles
and runs on JDK 26.

**6. The starter ships a `logs/log.out`.** It is not committed — `logs/` is gitignored,
as in labs 06 and 07, and the file is regenerated on every run.
