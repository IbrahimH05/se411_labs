# Lab 08 — Observer Pattern

SE411, Fall 2026/2027. An Observer framework: sensors publish readings, a dashboard
and a logger subscribe, and a cloned sensor starts life with no subscribers at all.

## Files

| File | What it is |
| --- | --- |
| [`pattern/Observer.java`](src/main/java/edu/spu/se411/lab08/pattern/Observer.java) | The interface, exactly as the handout specifies |
| [`pattern/Subject.java`](src/main/java/edu/spu/se411/lab08/pattern/Subject.java) | The interface, exactly as the handout specifies |
| [`pattern/AbstractSubject.java`](src/main/java/edu/spu/se411/lab08/pattern/AbstractSubject.java) | **Reusable base** — owns the observer list, implements all three methods, handles cloning |
| [`sensor/Sensor.java`](src/main/java/edu/spu/se411/lab08/sensor/Sensor.java) | Abstract sensor — name, reading, `setReading` that notifies |
| [`sensor/TemperatureSensor.java`](src/main/java/edu/spu/se411/lab08/sensor/TemperatureSensor.java) | °C, COLD / COMFORTABLE / HOT |
| [`sensor/HumiditySensor.java`](src/main/java/edu/spu/se411/lab08/sensor/HumiditySensor.java) | %, DRY / COMFORTABLE / HUMID |
| [`observers/SensorObserver.java`](src/main/java/edu/spu/se411/lab08/observers/SensorObserver.java) | **Reusable base** — narrows `Subject` to `Sensor` once |
| [`observers/DashboardObserver.java`](src/main/java/edu/spu/se411/lab08/observers/DashboardObserver.java) | Prints each reading |
| [`observers/LoggingObserver.java`](src/main/java/edu/spu/se411/lab08/observers/LoggingObserver.java) | Writes each reading to the log file |
| [`App.java`](src/main/java/edu/spu/se411/lab08/App.java) | The simulation loop, the unregister demo and the clone demo |
| [`ObserverPatternTest.java`](src/test/java/edu/spu/se411/lab08/ObserverPatternTest.java) | 19 tests |

## Running it

```bash
mvn clean package exec:java
```

That takes about 13 seconds — the handout's loop sleeps 1000 ms between each of the
ten readings. To watch it without the wait:

```bash
mvn exec:java -Dreading.interval.ms=0
```

The property defaults to the handout's 1000 ms, so the plain command above is
unchanged. Tested with Maven 3.9.16 and OpenJDK 26.0.2.1. `logs/` is gitignored.

---

## What the pattern does

A **Subject** keeps a list of **Observers** and tells them when something changed.
The point is the direction of the dependency: the sensor knows only that it holds
some `Observer`s. It has never heard of a dashboard or a logger, and adding a third
observer — an alarm, a database writer — requires no change to any sensor.

```
        register / unregister
   ┌──────────────────────────────┐
   │                              ▼
┌──────────────┐           ┌─────────────────┐
│   Observer   │◄──────────│     Subject     │
├──────────────┤  notify   ├─────────────────┤
│ update(Subj) │  all      │ register(Obs)   │
└──────────────┘           │ unregister(Obs) │
                           │ notifyObservers │
                           └─────────────────┘
```

### Push or pull

`update(Subject subject)` hands the observer the **whole subject**, not the new value.
That is the *pull* model: the observer decides what it wants and reads it back off the
sensor. A *push* model would be `update(double newReading)`.

Pull costs a cast — the observer receives `Subject` and needs a `Sensor` — but it means
the interface never changes when a subject grows a new field. `LoggingObserver` reads
the name, reading, unit **and** status from one `update` call; a push interface would
have needed four parameters or a new method.

---

## Class structure

```
        ┌───────────────┐                    ┌──────────────────┐
        │   «interface» │                    │   «interface»    │
        │    Subject    │                    │     Observer     │
        └───────────────┘                    └──────────────────┘
                △                                     △
                │ implements                          │ implements
        ┌───────────────────┐                ┌──────────────────────┐
        │  AbstractSubject  │                │   SensorObserver     │
        ├───────────────────┤                ├──────────────────────┤
        │ - observers:List  │                │ + update(Subject) ●  │ final
        │ + register()    ● │                │ # onReadingChanged ▲ │ abstract
        │ + unregister()  ● │                └──────────────────────┘
        │ + notifyObservers●│                           △
        │ # clone()       ● │                ┌──────────┴──────────┐
        └───────────────────┘                │                     │
                △                   ┌──────────────────┐ ┌──────────────────┐
                │                   │ DashboardObserver│ │ LoggingObserver  │
        ┌───────────────┐           └──────────────────┘ └──────────────────┘
        │ Sensor «abs»  │
        ├───────────────┤
        │ - name        │
        │ - reading     │
        │ + setReading()│
        │ + getUnit()  ▲│ abstract
        │ + getStatus()▲│ abstract
        └───────────────┘
                △
        ┌───────┴────────┐
┌──────────────────┐ ┌─────────────────┐
│TemperatureSensor │ │ HumiditySensor  │
└──────────────────┘ └─────────────────┘
```

## Code reuse

Two base classes carry everything that would otherwise be duplicated:

**`AbstractSubject`** implements all three `Subject` methods once. Any class that
wants to be observable extends it and inherits registration, de-registration,
notification and correct cloning. `Sensor` adds nothing to that machinery — it only
calls `notifyObservers()` when its value moves.

**`SensorObserver`** solves a subtler duplication. The interface delivers a `Subject`,
so every sensor observer would begin with the same narrowing:

```java
if (subject instanceof Sensor sensor) { ... }
```

It is written once in the base, which then calls an abstract
`onReadingChanged(Sensor)`. `update` is **final** so subclasses cannot accidentally
skip the check, and they receive a `Sensor` already. The update counter is inherited
too. This is the **template method** pattern sitting inside the observer pattern:
the base fixes the sequence, the subclass fills in the one varying step.

| Reused | Lives in | Used by |
| --- | --- | --- |
| Observer list and all three `Subject` methods | `AbstractSubject` | every subject |
| Clone-with-fresh-observers | `AbstractSubject` | every subject |
| `Subject` → `Sensor` narrowing | `SensorObserver` | both observers |
| Update counting | `SensorObserver` | both observers |
| `name`, `reading`, notify-on-change | `Sensor` | both sensor types |
| `describe()` output format | `Sensor` | both sensor types |

## Polymorphism

`Sensor` declares `getUnit()` and `getStatus()` abstract; the subclasses answer
differently, and neither observer knows which kind it is holding. The same number
means different things:

```java
for (Sensor sensor : List.of(temperature, humidity)) {
    sensor.setReading(50.0);
}
// temperature.getStatus() -> "HOT"
// humidity.getStatus()    -> "COMFORTABLE"
```

A third sensor type is one new subclass. The observers, `AbstractSubject` and the
simulation loop are untouched.

---

## Part 3 — cloning, and why it is the interesting part

> *"the new clone should not be observed by the same observers as the original, it
> will have its own observers"*

`Object.clone()` performs a **shallow copy**: every field is copied bit for bit. For
`observers`, the field is a *reference*, so the copy points at **the very same
`ArrayList`**. Without intervention:

```java
TemperatureSensor copy = original.clone();
copy.register(newObserver);     // the original is now notified too
copy.setReading(40.0);          // the original's observers all fire
```

Two "separate" sensors would share one subscriber list — the original would be
notified of readings belonging to the copy. The fix is one line:

```java
@Override
protected AbstractSubject clone() throws CloneNotSupportedException {
    AbstractSubject copy = (AbstractSubject) super.clone();
    copy.observers = new ArrayList<>();   // fresh and empty, not shared, not copied
    return copy;
}
```

Note **empty**, not a copy of the list: the requirement is that the clone is not
observed by the original's observers, so it starts with none and gathers its own.

### Two smaller decisions

**Covariant return types.** `AbstractSubject.clone()` returns `AbstractSubject`,
`Sensor` narrows it to `Sensor`, and each concrete sensor narrows it again:

```java
TemperatureSensor copy = temperature.clone();   // no cast at the call site
```

Java allows an override to return a subtype of what the parent returned. `super.clone()`
still produces the correct runtime class, so a cloned `HumiditySensor` really is a
`HumiditySensor` — there is a test for that.

**Dropping the checked exception.** `Object.clone()` declares
`CloneNotSupportedException`, which cannot occur here because `AbstractSubject`
implements `Cloneable`. `Sensor.clone()` catches it and rethrows an `AssertionError`,
so callers are not forced into a `try`/`catch` for something that cannot happen. An
override is permitted to declare *fewer* checked exceptions than the method it
overrides.

> `Cloneable` is a famously awkward API — a marker interface with no methods, paired
> with a `protected` `clone()` on `Object`. A copy constructor is usually the better
> choice in new code. It is used here because the handout asks for it, and it makes
> the shallow-copy lesson concrete.

---

## One more robustness detail

`notifyObservers()` iterates a **copy** of the list:

```java
for (Observer observer : new ArrayList<>(observers)) {
    observer.update(this);
}
```

Without the copy, an observer that unregisters itself inside its own `update` would
mutate the list mid-iteration and throw `ConcurrentModificationException`. That is a
realistic thing for an observer to do — "notify me once, then forget me" — and there
is a test for it.

---

## Logging

SLF4J over log4j, configured per the annex: `FileAppender` to
`./logs/App/log4j/log.out`, root level `INFO`, pattern `%d %p [%t] %C - %m%n`.
`LoggingObserver` is itself an observer, so the log is produced *by* the pattern
rather than bolted on beside it — readings outside the comfortable band log at `warn`,
the rest at `info`.

---

## Program output

Trimmed to the first two readings; the full run prints ten.

```text
== Simulating 10 sensor readings ==

reading 1
  [Dashboard] Temperature       33.84 °C  HOT
  [Logger   ] recorded Temperature = 33.84 °C (HOT)
  [Dashboard] Humidity          42.66 %   COMFORTABLE
  [Logger   ] recorded Humidity = 42.66 % (COMFORTABLE)

reading 2
  [Dashboard] Temperature       20.04 °C  COMFORTABLE
  [Logger   ] recorded Temperature = 20.04 °C (COMFORTABLE)
  [Dashboard] Humidity          45.82 %   COMFORTABLE
  [Logger   ] recorded Humidity = 45.82 % (COMFORTABLE)


  ... readings 3 to 10 ...
== Unregistering the dashboard ==
  Temperature now has 1 observer(s); only the logger should answer
  [Logger   ] recorded Temperature = 31.50 °C (HOT)

== Cloning the sensor ==
  original : Temperature = 31.50 °C (HOT), 2 observer(s)
  clone    : Temperature = 31.50 °C (HOT), 0 observer(s)
  clone is a TemperatureSensor and is a distinct object: true

  changing the clone - the original's observers must stay silent:
  (nothing above means the observer lists are genuinely separate)

  giving the clone its own observer:
  [Clone-DB ] Temperature       12.00 °C  COLD

  original still has 2 observer(s), clone has 1

Dashboard received 20 updates, Logger received 21.
```

The last three sections are the ones that prove the behaviour:

- **Unregistering** — the dashboard goes quiet, the logger keeps answering.
- **Cloning** — the clone copies the reading (`31.50 °C`) but reports **0 observers**,
  and changing it to `40.0` prints nothing at all, because the original's two
  observers are not attached to it.
- **Its own observers** — registering `Clone-DB` on the clone affects only the clone;
  the original still has 2, the clone has 1.

The final tally is a check in itself: ten readings across two sensors is 20 updates,
and the logger has 21 because it alone was still registered for the extra reading
taken while the dashboard was unregistered.

## Log file

```text
2026-10-04 09:20:39,772 INFO [main] App - Application is starting...
2026-10-04 09:20:39,773 INFO [main] App - Registered 2 observers on Temperature and 2 on Humidity
2026-10-04 09:20:39,773 WARN [main] observers.LoggingObserver - Temperature is HOT at 33.84 °C
2026-10-04 09:20:39,774 INFO [main] observers.LoggingObserver - Humidity reading changed to 42.66 %
2026-10-04 09:20:40,789 INFO [main] observers.LoggingObserver - Temperature reading changed to 20.04 °C
2026-10-04 09:20:40,791 INFO [main] observers.LoggingObserver - Humidity reading changed to 45.82 %

  ... one INFO or WARN per reading ...

2026-10-04 09:20:49,859 WARN [main] observers.LoggingObserver - Temperature is HOT at 31.50 °C
2026-10-04 09:20:49,861 INFO [main] App - Cloned Temperature: original has 2 observers, clone has 1
2026-10-04 09:20:49,861 INFO [main] App - Application is stopping...
```

25 lines: 18 `INFO`, 7 `WARN`, no errors. The timestamps are one second apart, which
is the `Thread.sleep(1000)` showing up in the evidence.

Nothing from the clone appears in the log — `LoggingObserver` was never registered on
it. That absence is the requirement being satisfied.

---

## Tests

```text
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Not required by the handout. The clone requirement in particular is the kind of thing
that *looks* right in a demo — a clone with its own observer prints what you expect —
while the sharing bug hides until something changes the original.

| Group | Tests |
| --- | --- |
| Notification | Every registered observer is notified; an unregistered one stops; registering twice notifies once; setting the same value again notifies nobody |
| Robustness | Null observer rejected; unregistering a stranger is harmless; an observer may unregister itself mid-notification; the observer list cannot be modified from outside |
| **Cloning** | **A clone starts with 0 observers; changing the clone never notifies the original's observers; changing the original never notifies the clone's; the two lists are separate objects; state is copied but the object is distinct; the runtime subclass is preserved** |
| Polymorphism | Each type reports its own unit and status; the same reading means different things to each; the COLD/HOT boundaries are exact |

The three "never notifies" tests are the ones that would fail against a plain
`super.clone()`, and they assert on an **absence** — that an observer heard nothing.
Absence is easy to miss by eye in a demo and trivial for a test to check.

---

## Notes

**1. The handout's loop is a `for`, not a `while`.** The prose asks for "a while loop
that simulates sensor readings changes" and then supplies a `for` loop. The supplied
code is used as written.

**2. `setReading` notifies only when the value actually moves.** The problem statement
says observers should be notified "when the sensor's reading value changes", so
setting the same value twice fires nothing. The simulation never hits this, since
every reading is random, but there is a test for it. If a grader expects notification
on every call, it is one `if` to remove in `Sensor.setReading`.

**3. The sleep is configurable.** `-Dreading.interval.ms=0` runs the simulation
instantly. The default is the handout's 1000 ms, so nothing changes unless the
property is set — it exists so the program can be verified without a 13-second wait.

**4. The package is `edu.spu.se411.lab08`,** taken from the annex's `mainClass`. Unlike
Lab 07's annex, this one names the correct lab. The `spu` spelling is the course
material's own; labs 03 and 05 use `psu`.

**5. `Cloneable` over a copy constructor.** A copy constructor
(`TemperatureSensor(TemperatureSensor other)`) would be the better choice in new code
and avoids `Cloneable` entirely. `clone()` is used because part 3 asks for it, and it
is what makes the shallow-copy problem visible.
