# Lab 01 — Introduction to git

SE411, Fall 2026/2027.

**Deliverable:** the link to the public repository created during this lab.

> ### https://github.com/IbrahimH05/se411_labs

That link *is* the submission — Lab 01 has no source code to hand in. This file is
the record of the four parts, with the commands that were actually run and their
real output.

---

## Part A — Configure git

Identity, set globally so every repository on this machine uses it:

```bash
git config --global user.name "Ibrahim"
git config --global user.email "your.email@example.com"
```

Verify (`--show-origin` also tells you *which* file a value came from, which is
the fastest way to debug a config that isn't taking effect):

```bash
git config --global --get user.name
git config --global --list --show-origin
```

The repository was then created on GitHub as **`se411_labs`**, public, per the
handout's note to add a README and a `.gitignore`:

- [`README.md`](../README.md) — what the repo is, how it's organized, how to run a lab
- [`.gitignore`](../.gitignore) — compiled Java (`*.class`, `out/`), IDE folders, `.DS_Store`

The `.gitignore` matters more than it looks in a Java course: without it every
`javac` run leaves `.class` files that git would happily commit as noise.

## Part B — Get the repository locally

The handout clones into a labs folder:

```bash
cd ~/Desktop
git clone https://github.com/IbrahimH05/se411_labs.git
```

This repo took the other route to the same place, because the local folder already
existed with work in it — `git init` plus wiring up the remote by hand:

```bash
git init -b main
git remote add origin https://github.com/IbrahimH05/se411_labs.git
```

`clone` is just those two steps plus a `fetch` and a checkout. The end state is
identical, which you can confirm from the remote:

```console
$ git remote -v
origin	https://github.com/IbrahimH05/se411_labs.git (fetch)
origin	https://github.com/IbrahimH05/se411_labs.git (push)
```

## Part C — Work on the repository

Edit files (`code .` opens VSCode on the current folder), then inspect what git
noticed:

```bash
git status
```

Modified-but-unstaged files are listed in red. Staging is a separate, deliberate
step — this is the part of git that most tutorials skip past too fast. A file has
three possible places to be: the **working tree** (your edits), the **staging
area**/index (what the next commit will contain), and the **repository** (committed
history). `add` moves working tree → staging; `commit` moves staging → repository.

```bash
git add README.md          # or: git add . for everything
git status                 # same files, now green = staged
git commit -m "Change the README"
```

Because staging is separate, you can commit *some* of your changes and leave the
rest — useful when one editing session touched two unrelated things.

## Part D — Work with the remote

Ask what "remote" actually means here:

```console
$ git branch -avv
  lab02                6b9baa8 [origin/lab02] Add lab02: Java generics exercises
* main                 6da2b15 [origin/main] Merge lab02 into main
  remotes/origin/lab02 6b9baa8 Add lab02: Java generics exercises
  remotes/origin/main  6da2b15 Merge lab02 into main
```

`origin` is the name for the GitHub URL; `origin/main` is a local, read-only
bookmark recording where GitHub's `main` was the last time we talked to it. That's
why it can be stale, and why comparing against it is meaningful:

```bash
git log origin/main..main    # commits I have that GitHub does not
git log main..origin/main    # commits GitHub has that I do not
git fetch                    # update the origin/* bookmarks without touching my work
```

Push, setting the upstream on the first push so later pushes need no arguments:

```bash
git push -u origin main
```

Refreshing the GitHub page then shows the new README. Current published history:

```console
$ git log --oneline --graph --all
*   6da2b15 Merge lab02 into main
|\
| * 6b9baa8 Add lab02: Java generics exercises
|/
* 9d12b0d Add repo scaffolding: README and .gitignore
```

The merge commit is the branch-per-lab workflow this repo uses: each lab is built
on its own branch (`lab01`, `lab02`, ...) and merged into `main`, so `main` holds
every lab side by side while each branch stays as a snapshot.

## Command reference

| Command | What it does |
| --- | --- |
| `git config --global user.name/.email` | Set the identity stamped on commits |
| `git clone <url>` | Copy a remote repo locally, wiring up `origin` |
| `git init -b main` / `git remote add origin <url>` | The manual equivalent of `clone` |
| `git status` | What's modified, staged, or untracked |
| `git add <file>` | Working tree → staging area |
| `git commit -m "msg"` | Staging area → local history |
| `git remote -v` | Which URLs `origin` points at |
| `git fetch` | Refresh `origin/*` bookmarks; changes nothing locally |
| `git log origin/main..main` | Commits not yet pushed |
| `git push -u origin main` | Publish, and remember the upstream |
| `git branch -avv` | All branches, local and remote, with tracking info |

## Note

The instructor's handout (`lab 01 git.docx`) is deliberately **not** committed —
`.gitignore` excludes `*.docx` so course material stays out of a public repo. The
file is still in the folder locally.
