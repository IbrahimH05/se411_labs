# SE411 Labs

Lab work for **SE411 — Fall 2026/2027**. Each lab is small and self-contained and
drills one concept from the course — generics, unit testing, build tooling. Early
labs run on a bare JDK; later ones are Maven projects.

## What's in here

| Lab | Topic | Code |
| --- | --- | --- |
| [lab01](lab01/) | Introduction to git — config, clone, staging, commits, remotes | walkthrough + command reference |
| [lab02](lab02/) | Java Generics — generic classes, bounded type parameters, a type-changing pipeline, wildcards | [lab02/src/lab02](lab02/src/lab02) |
| [lab03](lab03/) | JUnit intro — unit testing a generic `Stack` with JUnit 5 under Maven | [lab03/src](lab03/src) |
| [lab04](lab04/) | Maven intro — JavaFX dependency and plugin, plus the Maven Site plugin, for the FinTech main project | [lab04](lab04/) |

## How the repo is organized

Every lab gets its own top-level folder (`lab01/`, `lab02/`, ...) holding its
sources and a lab-specific README covering that lab's exercises and output. `main` accumulates all labs side by side, which is the layout the course
asks for (`project: se411_labs, folder: labNN`).

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
└── lab04/
    ├── README.md
    ├── pom.xml        <- Maven + JavaFX + Site plugin
    └── src/main/java/psu/se411/fintech/
```

## Running a lab

Labs 01–02 need nothing but a JDK; labs 03–04 are Maven projects. From the lab's
own directory:

| Lab | Command |
| --- | --- |
| lab02 | `javac -d out $(find src -name '*.java') && java -cp out lab02.Main` |
| lab03 | `mvn test` |
| lab04 | `mvn clean javafx:run` and `mvn clean site` |

Each lab's README repeats the exact command and shows the real output.

## Requirements

git and a JDK for the Java labs; labs 03–04 additionally need Maven. Everything
here was built and run against **OpenJDK 26.0.2.1** and **Maven 3.9.16**; each
lab's README notes what it was tested with. Check yours with:

```bash
git --version && javac -version && mvn -version
```
