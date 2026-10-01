# Lab 07 — Polymorphism in Java

SE411, Fall 2026/2027. A travel agency booking system: three kinds of booking, three
different price formulas, one method that prices any of them.

## Files

| File | What it is |
| --- | --- |
| [`model/Booking.java`](src/main/java/edu/spu/se411/lab07_polymorphism/model/Booking.java) | **Abstract base** — common fields, shared validation, shared `summary()` |
| [`model/FlightBooking.java`](src/main/java/edu/spu/se411/lab07_polymorphism/model/FlightBooking.java) | Base price + luggage × extra-luggage rate |
| [`model/TrainBooking.java`](src/main/java/edu/spu/se411/lab07_polymorphism/model/TrainBooking.java) | Distance × seat-class rate |
| [`model/CarRentalBooking.java`](src/main/java/edu/spu/se411/lab07_polymorphism/model/CarRentalBooking.java) | Daily rate × days |
| [`model/SeatClass.java`](src/main/java/edu/spu/se411/lab07_polymorphism/model/SeatClass.java) | Enum carrying each class's rate per km |
| [`config/TravelConfig.java`](src/main/java/edu/spu/se411/lab07_polymorphism/config/TravelConfig.java) | Global configuration constants |
| [`exceptions/BookingException.java`](src/main/java/edu/spu/se411/lab07_polymorphism/exceptions/BookingException.java) | Shared base for both custom exceptions |
| [`exceptions/MissingInformationException.java`](src/main/java/edu/spu/se411/lab07_polymorphism/exceptions/MissingInformationException.java) | A required value was never supplied |
| [`exceptions/InvalidArgumentException.java`](src/main/java/edu/spu/se411/lab07_polymorphism/exceptions/InvalidArgumentException.java) | A value was supplied but is out of range |
| [`service/BookingService.java`](src/main/java/edu/spu/se411/lab07_polymorphism/service/BookingService.java) | `computeTotalPrice(Booking)` — works for any subtype |
| [`App.java`](src/main/java/edu/spu/se411/lab07_polymorphism/App.java) | Main program, with start/stop and exception logging |
| [`log4j.properties`](src/main/resources/log4j.properties) | File logger configuration |
| [`BookingTest.java`](src/test/java/edu/spu/se411/lab07_polymorphism/BookingTest.java) | 20 tests |

## Running it

```bash
mvn clean package exec:java
```

```bash
cat logs/App/log4j/log.out
```

```bash
mvn test
```

Tested with Maven 3.9.16 and OpenJDK 26.0.2.1. `logs/` is gitignored.

---

## Class design

```
                        ┌──────────────────────────┐
                        │   Booking  «abstract»    │
                        ├──────────────────────────┤
                        │ - bookingId              │
                        │ - customerName           │
                        │ - travelDate             │
                        │ - destinationCity        │
                        ├──────────────────────────┤
                        │ + computeTotalPrice()  ▲ │  abstract
                        │ + getBookingType()     ▲ │  abstract
                        │ + describeDetails()    ▲ │  abstract
                        │ + summary()            ● │  final, inherited
                        │ # requireProvided()    ● │  inherited helper
                        │ # requireInRange()     ● │  inherited helper
                        └──────────────────────────┘
                                     △
            ┌────────────────────────┼────────────────────────┐
            │                        │                        │
  ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────────┐
  │  FlightBooking   │   │   TrainBooking   │   │  CarRentalBooking    │
  ├──────────────────┤   ├──────────────────┤   ├──────────────────────┤
  │ - basePrice      │   │ - seatClass      │   │ - dailyRate          │
  │ - luggageWeight? │   │ - distanceKm?    │   │ - rentalDays?        │
  └──────────────────┘   └──────────────────┘   └──────────────────────┘

  ?  = nullable: supplied after the booking is created
```

**Abstract class, not an interface.** The three types share *state* (booking id,
customer, date, destination) and *behaviour* (validation, `summary()`). An interface
carries neither, so every subclass would redeclare the four fields and re-implement
the same checks. Code reuse was an explicit requirement, and inheritance of state is
what delivers it here.

---

## The polymorphism

`Booking` declares the operation without implementing it:

```java
public abstract double computeTotalPrice() throws MissingInformationException, InvalidArgumentException;
```

Each subclass supplies its own formula. `BookingService` then prices *any* booking
without knowing which kind it has:

```java
public double computeTotalPrice(Booking booking) throws BookingException {
    return booking.computeTotalPrice();   // resolved at runtime
}
```

This is the lab's stated requirement — a method taking `Booking` that works for every
type — and `App` exercises it over a mixed list:

```java
for (Booking booking : bookings) {
    double total = service.computeTotalPrice(booking);
}
```

One loop, one call, three different formulas. The JVM picks the implementation from
the **runtime** type of each object, not the `Booking` type of the variable. Adding a
fourth booking type (a ferry, say) means writing one new subclass — `BookingService`,
`App` and the loop above are untouched. That is the payoff.

### Polymorphism instead of conditionals, twice

**`SeatClass` carries its own rate.** The obvious implementation of train pricing is
a conditional:

```java
if (seatClass == SeatClass.FIRST_CLASS) {       // not this
    return distanceKm * TravelConfig.TRAIN_FIRST_CLASS_RATE;
} else {
    return distanceKm * TravelConfig.TRAIN_STANDARD_RATE;
}
```

Instead the rate is attached to the enum constant:

```java
public enum SeatClass {
    STANDARD(TravelConfig.TRAIN_STANDARD_RATE),
    FIRST_CLASS(TravelConfig.TRAIN_FIRST_CLASS_RATE);
}
```

so `TrainBooking` reduces to `distanceKm * seatClass.getRatePerKm()`. A new seat class
is one enum constant and no change to the pricing code — the same argument as the
class hierarchy, one level down.

**`summary()` is `final` and inherited.** It is written once in `Booking` and calls
the abstract `getBookingType()` and `describeDetails()`. The shared format lives in
one place; each subclass contributes only its own fragment.

---

## Code reuse

| Reused | Where it lives | Who uses it |
| --- | --- | --- |
| Booking id, customer, date, destination | `Booking` fields | all three subtypes |
| Blank/null checks on common fields | `Booking` constructor | all three |
| `requireProvided(...)` | `Booking`, protected | all three |
| `requireInRange(...)` | `Booking`, protected | all three |
| `summary()` output format | `Booking`, final | all three |
| `formatMoney(...)` | `Booking`, protected static | all three |
| Exception message construction | `BookingException` subclasses | every throw site |

Without the two protected helpers, each subclass would write its own null check and
its own range comparison — three chances to get a boundary wrong, and three places to
edit when the message format changes.

---

## The two custom exceptions

```
BookingException  «abstract, checked»
├── MissingInformationException   — the value was never supplied
└── InvalidArgumentException      — the value was supplied but is out of range
```

A **shared base** means a caller that does not care which went wrong writes one
`catch (BookingException e)`, while a caller that does care can still catch the
specific type. `computeTotalPrice()` needs only `throws BookingException` in
`BookingService`.

Both are **checked** (`extends Exception`): a missing luggage weight is an expected
state of a booking that is still being filled in, not a programming bug, so the
compiler should force callers to deal with it. Genuine programming errors — a null
booking, a negative base price, a blank customer name — throw unchecked
`IllegalArgumentException` instead. The distinction is deliberate and the same one
used in Lab 05.

Both carry **structured data**, not just a message: `getBookingId()`,
`getFieldName()`, and for `InvalidArgumentException` also `getValue()`, `getMin()`
and `getMax()`. A caller can react to the numbers without parsing text.

### Missing and invalid happen at different moments

This is the design decision worth defending. The spec says a value that is absent
throws one exception and a value out of range throws the other. Those are two
different *times*:

| | When | Exception |
| --- | --- | --- |
| **Out of range** | At the setter, the moment the value arrives | `InvalidArgumentException` |
| **Not provided** | At `computeTotalPrice()`, when it is finally needed | `MissingInformationException` |

Rejecting a bad value in the setter means an out-of-range number is **never stored**,
so the object cannot hold invalid state. Presence can only be checked when the value
is actually required, because "not entered yet" is a legitimate state for a booking
that is still being assembled.

`computeTotalPrice()` re-checks the range as well. That is redundant given the setter
— deliberately, so the rule still holds for any future subclass that sets the field by
another route, and so the method satisfies either reading of the spec.

### Why nullable wrapper types

`Double luggageWeightKg`, not `double`. A primitive `double` always holds a value, so
"not provided" would have to be faked with a sentinel like `-1`, which is
indistinguishable from real data and silently breaks the range check. `null` says
exactly what it means.

---

## Global configuration

`TravelConfig` holds every rate and bound as a `public static final` constant, with a
private constructor that throws — it is a constant holder, never an instance. Every
limit the business rules mention is there, so changing the extra-luggage rate or the
maximum rental period is a one-line edit that the tests pick up automatically (they
assert against the constants, not against hard-coded numbers).

---

## Logging

SLF4J over log4j, configured exactly as the annex specifies: `FileAppender` to
`./logs/App/log4j/log.out`, pattern `%d %p [%t] %C - %m%n`, root level `INFO`.
Requirement (f) asks for start and stop events and raised exceptions:

| Event | Level |
| --- | --- |
| Application starting / stopping | `info` |
| Every exception caught in `App` | `error`, with the throwable passed as the last argument so the stack trace is kept |
| Booking skipped from revenue | `warn` |
| Individual price computations | `debug` |

There is no console appender, so `mvn exec:java` prints only the program's own
`System.out` — the log goes to the file.

---

## Program output

```text
== Bookings priced through Booking.computeTotalPrice() ==
  FL-1001 Flight     Ibrahim Alhalaki      2143.75 USD
  TR-2001 Train      Muhammad Abanmi        237.00 USD
  CR-3001 Car rental Layla Nasser           870.00 USD
  TR-2002 Train      Omar Khalid        no price: Booking TR-2002: Distance has not been provided yet

== Summary ==
  priceable bookings : 3 of 4
  total revenue      : 3250.75 USD

== Full manifest (Booking.summary(), shared by every subtype) ==
  [FL-1001] Flight | Ibrahim Alhalaki | Istanbul on 2026-11-12 | base 1850.00 USD, luggage 23.5 kg | total 2143.75 USD
  [TR-2001] Train | Muhammad Abanmi | Riyadh on 2026-11-20 | FIRST_CLASS at 0.60/km, distance 395.0 km | total 237.00 USD
  [CR-3001] Car rental | Layla Nasser | Dubai on 2026-12-03 | 145.00 USD/day, 6 days | total 870.00 USD
  [TR-2002] Train | Omar Khalid | Dammam on 2026-12-15 | STANDARD at 0.35/km, distance not provided | total unavailable (Booking TR-2002: Distance has not been provided yet)

== Business rules enforced ==
  flight with no luggage weight entered         -> MissingInformationException
  luggage weight of 55 kg (max is 40)           -> InvalidArgumentException
  train distance of 2500 km (max is 2000)       -> InvalidArgumentException
  rental of 0 days (min is 1)                   -> InvalidArgumentException
  car rental with no days entered               -> MissingInformationException
```

## Log file at `INFO`

```text
2026-10-01 11:02:32,866 INFO [main] App - Application is starting...
2026-10-01 11:02:32,869 ERROR [main] App - Could not price booking TR-2002
2026-10-01 11:02:32,870 WARN [main] service.BookingService - Skipping TR-2002 in revenue total: Booking TR-2002: Distance has not been provided yet
2026-10-01 11:02:32,872 ERROR [main] App - Rejected: flight with no luggage weight entered
2026-10-01 11:02:32,874 ERROR [main] App - Rejected: luggage weight of 55 kg (max is 40)
2026-10-01 11:02:32,874 ERROR [main] App - Rejected: train distance of 2500 km (max is 2000)
2026-10-01 11:02:32,875 ERROR [main] App - Rejected: rental of 0 days (min is 1)
2026-10-01 11:02:32,875 ERROR [main] App - Rejected: car rental with no days entered
2026-10-01 11:02:32,875 INFO [main] App - Application is stopping...
```

Stack trace lines are in the file but omitted here for readability — each `ERROR`
carries the full trace, because the throwable is passed as the last argument rather
than concatenated into the message.

## Log file at `DEBUG`

Changing one word on the first line of `log4j.properties` opens up the service layer:

```text
2026-10-01 11:02:28,933 INFO [main] App - Application is starting...
2026-10-01 11:02:28,934 DEBUG [main] App - Built 4 sample bookings
2026-10-01 11:02:28,935 DEBUG [main] service.BookingService - Computed total for FL-1001 (Flight): 2143.75
2026-10-01 11:02:28,935 DEBUG [main] service.BookingService - Computed total for TR-2001 (Train): 237.0
2026-10-01 11:02:28,936 DEBUG [main] service.BookingService - Computed total for CR-3001 (Car rental): 870.0
2026-10-01 11:02:28,937 ERROR [main] App - Could not price booking TR-2002
2026-10-01 11:02:28,938 DEBUG [main] service.BookingService - Computed total for FL-1001 (Flight): 2143.75
2026-10-01 11:02:28,939 DEBUG [main] service.BookingService - Computed total for TR-2001 (Train): 237.0
2026-10-01 11:02:28,939 DEBUG [main] service.BookingService - Computed total for CR-3001 (Car rental): 870.0
2026-10-01 11:02:28,939 DEBUG [main] service.BookingService - TR-2002 is not priceable yet: Booking TR-2002: Distance has not been provided yet
2026-10-01 11:02:28,939 DEBUG [main] service.BookingService - Computed total for FL-1001 (Flight): 2143.75
2026-10-01 11:02:28,939 DEBUG [main] service.BookingService - Computed total for TR-2001 (Train): 237.0
2026-10-01 11:02:28,939 DEBUG [main] service.BookingService - Computed total for CR-3001 (Car rental): 870.0
2026-10-01 11:02:28,939 WARN [main] service.BookingService - Skipping TR-2002 in revenue total: Booking TR-2002: Distance has not been provided yet
2026-10-01 11:02:28,940 ERROR [main] App - Rejected: flight with no luggage weight entered
2026-10-01 11:02:28,942 ERROR [main] App - Rejected: luggage weight of 55 kg (max is 40)
2026-10-01 11:02:28,942 ERROR [main] App - Rejected: train distance of 2500 km (max is 2000)
2026-10-01 11:02:28,943 ERROR [main] App - Rejected: rental of 0 days (min is 1)
2026-10-01 11:02:28,943 ERROR [main] App - Rejected: car rental with no days entered
2026-10-01 11:02:28,943 INFO [main] App - Application is stopping...
```

---

## Tests

```text
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Not required by the handout, but the business rules here are almost entirely
boundaries, and boundaries are where this kind of code actually breaks.

| Group | Tests |
| --- | --- |
| Polymorphism | One loop prices all three subtypes correctly; each reports its own type name; inherited `summary()` uses subclass details |
| Flight | Price formula; **0 kg and 40 kg both accepted**; 40.1 and −0.1 rejected; missing weight reports the right booking id and field |
| Train | `FIRST_CLASS` costs more than `STANDARD` over the same distance; **1 km and 2000 km both accepted**; 0 and 2000.1 rejected |
| Car rental | Rate × days; **1 and 30 days both accepted**; 0 and 31 rejected |
| Exceptions | Both types catch as `BookingException`; `InvalidArgumentException` carries value, min and max |
| Inherited validation | Blank id, blank name, null date and negative rate all rejected, for every subtype |
| Service | Revenue skips unpriceable bookings rather than failing; null booking rejected |

The boundary tests matter most: `<` versus `<=` is the single most likely bug in this
lab, and nothing in the program output would reveal it. Each asserts against
`TravelConfig` constants rather than literals, so changing a limit does not quietly
invalidate the test.

---

## Notes and findings

**1. The DEBUG log exposed a real inefficiency.** At `DEBUG` the service logs each
computation, and `FL-1001` appears **three times**: once in the pricing loop, once in
`findPriceable`, once in `computeTotalRevenue`. Each walks the list and re-prices
independently. Harmless at four bookings, and the methods are genuinely independent,
but it is exactly the kind of repeated work that is invisible until you log it. Left
as-is and recorded here rather than silently restructured — caching the result would
be the fix if the list grew or pricing became expensive.

**2. The annex's `mainClass` points at Lab 06.** It reads
`edu.spu.se411.lab06_logging.App`, carried over from the previous handout. Copied
verbatim, `exec:java` would fail with a `ClassNotFoundException`. The POM here points
at `edu.spu.se411.lab07_polymorphism.App`.

**3. The package says `spu`, not `psu`.** Both the Lab 06 starter and this annex use
`edu.spu.se411`, so that spelling is kept for consistency with the material. Labs 03
and 05 use `edu.psu.se411`. Worth knowing the inconsistency is upstream, not a typo
introduced here.

**4. `mvn package` runs the tests, and they share the log file.** Because `package`
runs Surefire before `exec:java`, and `FileAppender` appends, a `DEBUG` run leaves the
tests' own log output interleaved with the application's. The `DEBUG` sample above was
captured with `mvn package -DskipTests` followed by `exec:java` on a cleared `logs/`
directory. Same root cause as Lab 06's no-rotation note: `mvn clean` removes `target/`
but knows nothing about `logs/`.

**5. Money is `double`.** Lab 05 argued for `BigDecimal`, and that argument still
holds — `double` cannot represent most decimal fractions exactly. It is used here
because every formula in this handout is a rate multiplied by a quantity and the lab's
subject is polymorphism, not currency arithmetic. Totals are rounded only at display
via `formatMoney`. For a system that actually held money, `BigDecimal` or integer
minor units would be correct.

**6. `computeTotalPrice()` declares two exceptions, `BookingService` declares one.**
The subclasses declare `MissingInformationException, InvalidArgumentException`
explicitly so the two failure modes are visible in the signature; the service widens
to the shared `BookingException` because it has no reason to distinguish them. Both
are correct, at different levels of abstraction.
