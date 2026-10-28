# SE411 Labs

Lab work for **SE411 — Fall 2026/2027**. Each lab is small and self-contained and
drills one concept from the course — generics, unit testing, build tooling,
exception handling, logging, polymorphism, design patterns. Early labs run on a bare JDK; later ones are Maven projects.

## What's in here

| Lab | Topic | Code |
| --- | --- | --- |
| [lab01](lab01/) | Introduction to git — config, clone, staging, commits, remotes | walkthrough + command reference |
| [lab02](lab02/) | Java Generics — generic classes, bounded type parameters, a type-changing pipeline, wildcards | [lab02/src/lab02](lab02/src/lab02) |
| [lab03](lab03/) | JUnit intro — unit testing a generic `Stack` with JUnit 5 under Maven | [lab03/src](lab03/src) |
| [lab04](lab04/) | Maven intro — JavaFX dependency and plugin, plus the Maven Site plugin, for the FinTech main project | [lab04](lab04/) |
| [lab05](lab05/) | Exception handling — custom checked exceptions, and an online wallet that refuses to overdraw | [lab05/src](lab05/src) |
| [lab06](lab06/) | Logging with SLF4J — the façade/binding split, swapping logback for log4j, levels and appenders | [lab06/src](lab06/src) |
| [lab07](lab07/) | Polymorphism — a travel booking system with an abstract base class, three price formulas and one pricing method | [lab07/src](lab07/src) |
| [lab08](lab08/) | Observer pattern — sensors as subjects, a dashboard and logger as observers, and cloning that does not share subscribers | [lab08/src](lab08/src) |
| [lab09](lab09/) | Strategy pattern — a generic `Inventory<T>` with pluggable search behaviour and wildcard display | [lab09/src](lab09/src) |
| [lab11](lab11/) | Introduction to threads — the double spending problem and the `synchronized` fix | [lab11/src](lab11/src) |
| [lab12](lab12/) | Threads and locks — fixed vs cached pools, deadlock, and resource ordering | [lab12/src](lab12/src) |
| [lab13](lab13/) | Spring introduction — a read-only REST API over H2 with Spring Data JDBC and validation | [lab13/src](lab13/src) |

## How the repo is organized

Every lab gets its own top-level folder (`lab01/`, `lab02/`, ...) holding its
sources and a lab-specific README covering that lab's exercises and output.
`main` accumulates all labs side by side, which is the layout the course asks for
(`project: se411_labs, folder: labNN`).

Each lab is also developed on its own branch (`lab01`, `lab02`, ...) and then
merged into `main`. The branch stays behind as a snapshot of that lab as it was
submitted; `main` is always the full picture.

```
se411_labs/
├── README.md          <- you are here
├── .gitignore
├── lab01/
│   └── README.md      <- git walkthrough + command reference (no code to submit)
├── lab02/
│   ├── README.md      <- exercises, design notes, expected output
│   └── src/lab02/     <- plain javac sources (package lab02)
├── lab03/
│   ├── README.md
│   ├── pom.xml        <- Maven + JUnit 5
│   └── src/{main,test}/java/edu/psu/se411/
├── lab04/
│   ├── README.md
│   ├── pom.xml        <- Maven + JavaFX + Site plugin
│   └── src/main/java/psu/se411/fintech/
├── lab05/
│   ├── README.md
│   ├── pom.xml        <- Maven + exec plugin + JUnit 5
│   └── src/{main,test}/java/edu/psu/se411/
├── lab06/
│   ├── README.md
│   ├── pom.xml        <- Maven + SLF4J + log4j binding
│   └── src/main/
│       ├── java/edu/spu/se411/lab06_logging/
│       └── resources/log4j.properties
├── lab07/
│   ├── README.md
│   ├── pom.xml        <- Maven + SLF4J + log4j + JUnit 5
│   └── src/
│       ├── main/java/edu/spu/se411/lab07_polymorphism/
│       ├── main/resources/log4j.properties
│       └── test/java/edu/spu/se411/lab07_polymorphism/
├── lab08/
│   ├── README.md
│   ├── pom.xml        <- Maven + SLF4J + log4j + JUnit 5
│   └── src/
│       ├── main/java/edu/spu/se411/lab08/
│       ├── main/resources/log4j.properties
│       └── test/java/edu/spu/se411/lab08/
└── lab09/
    ├── README.md
    ├── pom.xml        <- Maven + SLF4J + log4j + JUnit 5
    └── src/
        ├── main/java/edu/psu/se411/lab09_inventory/
        ├── main/resources/log4j.properties
        └── test/java/edu/psu/se411/lab09_inventory/
```

## Running a lab

Labs 01–02 need nothing but a JDK; labs 03–09 are Maven projects. From the lab's
own directory:

| Lab | Command |
| --- | --- |
| lab02 | `javac -d out $(find src -name '*.java') && java -cp out lab02.Main` |
| lab03 | `mvn test` |
| lab04 | `mvn clean javafx:run` and `mvn clean site` |
| lab05 | `mvn clean package exec:java` and `mvn test` |
| lab06 | `mvn clean package exec:java`, then `cat logs/App/log4j/log.out` |
| lab07 | `mvn clean package exec:java`, then `cat logs/App/log4j/log.out` |
| lab08 | `mvn clean package exec:java` (add `-Dreading.interval.ms=0` to skip the sleeps) |
| lab09 | `mvn clean package exec:java`, then `cat logs/log.out` |
| lab11 | `mvn clean package exec:java` |
| lab12 | `mvn clean package exec:java`; other mains via `-Dexec.mainClass=` |
| lab13 | `mvn clean spring-boot:run`, then `curl localhost:8080/api/v1/products` |

Each lab's README repeats the exact command and shows the real output.

## Requirements

git and a JDK for the Java labs; labs 03–09 additionally need Maven. Everything
here was built and run against **OpenJDK 26.0.2.1** and **Maven 3.9.16**; each
lab's README notes what it was tested with. Check yours with:

```bash
git --version && javac -version && mvn -version
```
