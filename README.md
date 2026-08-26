# SE411 Labs

Lab work for **SE411 — Fall 2026/2027**. Each lab is small and self-contained and
drills one concept from the course. The Java labs are written to compile and run
with nothing but a JDK — no Maven, no Gradle.

## What's in here

| Lab | Topic | Code |
| --- | --- | --- |
| [lab01](lab01/) | Introduction to git — config, clone, staging, commits, remotes | walkthrough + command reference |
| [lab02](lab02/) | Java Generics — generic classes, bounded type parameters, a type-changing pipeline, wildcards | [lab02/src/lab02](lab02/src/lab02) |

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

git, and a JDK 8 or newer for the Java labs (each lab's README notes the JDK it
was tested against). Check yours with `git --version` and `javac -version`.
