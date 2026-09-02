# Lab 04 — Maven Intro

SE411, Fall 2026/2027. Preparing the Maven project for the course main project:
wiring in JavaFX, then generating a documentation website with the Maven Site
plugin.

## Files

| File | What it is |
| --- | --- |
| [`pom.xml`](pom.xml) | The whole lab, really — dependency, plugins, and project metadata |
| [`src/main/java/psu/se411/fintech/MainClass.java`](src/main/java/psu/se411/fintech/MainClass.java) | A minimal JavaFX `Application` |

Coordinates: `psu.se411:fintech`, package `psu.se411.fintech` — the namespace
pattern the handout asks for. The project is named **FinTech**, after the course
main project.

## Running it

```bash
mvn clean javafx:run
```

```bash
mvn clean site
```

Then open `target/site/index.html`. Tested with Maven 3.9.16 and OpenJDK 26.0.2.1.

---

## Exercise 1 — Maven project dependencies

### The dependency

```xml
<dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-controls</artifactId>
  <version>23</version>
</dependency>
```

This is what puts the JavaFX classes on the compile classpath, so
`import javafx.application.Application` resolves. `javafx-controls` transitively
pulls `javafx-base` and `javafx-graphics`, which is why one entry is enough for a
window with a label.

JavaFX also ships **native** libraries, and which ones you need depends on the
machine. Maven resolves that automatically from your OS and architecture — on this
machine it fetched the `mac-aarch64` classifier. That's why the same POM works on
Windows and Linux without edits.

### The plugin

```xml
<plugin>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-maven-plugin</artifactId>
  <version>0.0.8</version>
  <configuration>
    <mainClass>psu.se411.fintech.MainClass</mainClass>
  </configuration>
</plugin>
```

The dependency alone is not enough to *run* the app. JavaFX is modular, and
launching it needs `--module-path` and `--add-modules` flags pointing at those
native jars. This plugin's `run` goal computes and applies them, which is what
`mvn javafx:run` does. `<mainClass>` must be the fully-qualified class name, and
must match the actual package — a mismatch here is the most common reason
`javafx:run` fails.

### The application class

```java
public class MainClass extends Application {
    public static void main(String[] args) { launch(); }

    @Override
    public void start(Stage primaryStage) { ... primaryStage.show(); }
}
```

Two required pieces. `launch()` starts the JavaFX runtime and its rendering thread;
`start(Stage)` is the callback it invokes once that's ready, on the JavaFX
Application Thread. A stage is the window, a scene is its contents. Nothing appears
without `show()` — a stage built but never shown is the other classic silent
failure.

`start` is wrapped in try/catch per the handout. Worth knowing that an exception
escaping `start` would otherwise be swallowed by the framework thread rather than
crashing `main` visibly.

### Verification

`mvn clean compile` succeeds (`javac [debug release 21]`), and `mvn javafx:run`
reaches the `run` goal and stays alive with no stack trace — meaning the toolkit
initialized and `start()` completed. The process was terminated deliberately
afterwards, so the exit code in the log is a SIGTERM, not a crash.

`maven.compiler.release` is set to 21 because JavaFX 23 is built for Java 21+;
without it Maven's default compiler level is far older and the build fails on the
JavaFX classes.

---

## Exercise 2 — Maven Site plugin

### Project metadata

Everything the site displays comes from POM elements, not from any separate config:

| Element | Feeds |
| --- | --- |
| `<name>`, `<description>`, `<url>` | The "About" section on `index.html` |
| `<licenses>` | `licenses.html` |
| `<organization>` | Header and About |
| `<developers>` | `team.html` |

### The plugins

```xml
<build><plugins>
  <plugin>
    <artifactId>maven-site-plugin</artifactId>
    <version>3.21.0</version>
  </plugin>
</plugins></build>

<reporting><plugins>
  <plugin>
    <artifactId>maven-project-info-reports-plugin</artifactId>
    <version>3.9.0</version>
  </plugin>
</plugins></reporting>
```

The split matters and is easy to get wrong. `maven-site-plugin` under `<build>`
only *renders* the site — on its own it produces a nearly empty shell. The pages
that actually carry your project information come from
`maven-project-info-reports-plugin`, and report plugins go under `<reporting>`, not
`<build>`. This is the optional "improve your generated site" step from the handout:
declaring the reports explicitly is what turns the site from a stub into real
documentation.

Both versions are pinned. Left unpinned, a Maven upgrade silently changes your
build output.

### Verification

`mvn clean site` reports `BUILD SUCCESS`, detects 15 available reports and renders
9 pages into `target/site/`:

```text
index.html          dependencies.html      plugin-management.html
summary.html        dependency-info.html   plugins.html
team.html           licenses.html          project-info.html
```

Spot-checked in the generated HTML rather than assumed:

| Expected | Where | Result |
| --- | --- | --- |
| `FinTech` | `index.html` | found |
| `section 1383` | `index.html` | found |
| `SE411-Tech-Company` | `index.html` | found |
| `Apache License 2.0` | `licenses.html` | found |
| `Ibrahim Alhalaki` and his email | `team.html` | found, in the Members table with organization and role |

`target/` is gitignored, so the site is not committed — it is regenerated with
`mvn clean site`.

---

## Renaming the project later

If the project name changes, the identifier appears in four places — `<artifactId>`,
`<mainClass>`, the `package` line, and the folder name. From this directory:

```bash
NEW=newname && mkdir -p src/main/java/psu/se411/$NEW && git mv src/main/java/psu/se411/fintech/MainClass.java src/main/java/psu/se411/$NEW/ && rmdir src/main/java/psu/se411/fintech && sed -i '' "s/fintech/$NEW/g" pom.xml src/main/java/psu/se411/$NEW/MainClass.java && mvn clean compile
```

Then update `<name>` and `<description>` by hand, since those are prose rather than
identifiers.
