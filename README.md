# SE411 Labs

Lab work for **SE411 — Fall 2026/2027**. Each lab is a small, self-contained Java
project that drills one language or design concept from the course, written to be
compiled and run with nothing but a JDK.

## What's in here

| Lab | Topic | Code |
| --- | --- | --- |
| [lab02](lab02/) | Java Generics — generic classes, bounded type parameters, a type-changing pipeline, wildcards | [lab02/src/lab02](lab02/src/lab02) |

## How the repo is organized

Every lab gets its own top-level folder (`lab02/`, `lab03/`, ...) holding its
sources and a lab-specific README that lists the exercises and the expected
output. `main` accumulates all labs side by side, which is the layout the course
asks for (`project: se411_labs, folder: labNN`).

Each lab is also developed on its own branch (`lab02`, `lab03`, ...) and then
merged into `main`. The branch stays behind as a snapshot of that lab as it was
submitted; `main` is always the full picture.

```
se411_labs/
├── README.md          <- you are here
├── .gitignore
└── lab02/
    ├── README.md      <- exercises, design notes, expected output
    └── src/lab02/     <- Java sources (package lab02)
```

## Running a lab

No Maven, no Gradle — plain `javac`. Sources live under `src/`, and each lab's
package matches its folder name, so from the lab directory:

```bash
javac -d out $(find src -name '*.java') && java -cp out lab02.Main
```

Each lab's README repeats the exact command for that lab.

## Requirements

A JDK 8 or newer (developed and tested against the JDK noted in each lab's
README). Check yours with `javac -version`.
