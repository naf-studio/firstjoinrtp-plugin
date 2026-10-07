# Contributing to FirstJoinRTP Plugin

Thank you for your interest in contributing to the firstjoinrtp-plugin project at NAF Studio. This document outlines our engineering standards, contribution workflow, and Bukkit/Paper architectural conventions.

---

## 1. Branching Strategy & Workflow

This project adheres to a streamlined Trunk-based Development model:

- `main`: The stable production branch. All modifications targeting `main` must be submitted via a Pull Request (PR) and pass continuous integration checks.
- Working branches should be branched directly from `main` using structured naming:
  - `feat/<short-description>`: New functionality, listeners, or configuration options.
  - `fix/<short-description>`: Bug fixes, teleportation edge cases, or exception handling.
  - `refactor/<short-description>`: Internal code refactoring or performance optimizations.
  - `chore/<short-description>`: Build tool updates, dependency updates, or CI maintenance.
  - `docs/<short-description>`: Documentation and guide revisions.

---

## 2. Commit Standards

### 2.1. Conventional Commits

All commit messages must adhere to the Conventional Commits specification:

```text
<type>(<scope>): <short description in lowercase>
```

- Allowed Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.
- Optional Scope: Component or module name (e.g., `listener`, `config`, `teleport`, `build`).
- Description: Concise imperative sentence in lowercase without trailing punctuation.

### 2.2. Atomic Commits

- Each commit must address a single logical concern.
- Never mix formatting changes, dependency updates, and gameplay logic within the same commit.
- Every commit must leave the codebase in a compilable state.

---

## 3. Code Style & Documentation Standards

### 3.1. Build Tooling & Verification Pipeline

- Build Tool: Apache Maven (bundled via `./mvnw` and `.\mvnw.cmd`).
- Target Runtime: Java 17 bytecode format (`--release 17`), maintaining compatibility with Java 17 through Java 25.
- Verify compilation locally prior to submitting changes:

  ```bash
  ./mvnw clean package
  ```

  On Windows:

  ```cmd
  .\mvnw.cmd clean package
  ```

### 3.2. Comment Hygiene & Javadoc

- Write clean, self-documenting code. Avoid trivial line-by-line comments (e.g., `// loop through players`, `// cancel event`).
- Standard Javadoc is required on all classes and public methods:
  - Concise imperative summary in the first line.
  - Detailed `@param` and `@return` tags where applicable.

---

## 4. Minecraft Architecture & Compatibility Principles

- Maintain the Spigot API abstraction as the primary dependency, ensuring out-of-the-box support across Spigot, Paper, Purpur, Gale, and LeafMC servers running Minecraft 1.20.4 through 1.21.x.
- Always check `player.isOnline()` in delayed runnables to avoid operating on disconnected sessions.
- Ensure all tracking sets and scheduled tasks are properly flushed in `onDisable()` to prevent memory leaks during server reloads.
- Utilize Bukkit's native `PersistentDataContainer` with namespaced keys rather than external file persistence for player teleport status.

---

## 5. Pull Request Process

1. Ensure your feature branch is rebased on top of the latest `main`.
2. Verify local compilation succeeds with `./mvnw clean package` (or `.\mvnw.cmd clean package`).
3. Open a Pull Request on GitHub. The pre-configured template will be populated automatically.
4. Provide a clear summary of your changes and reference relevant issues (e.g., `Closes #12`).
5. Merging requires passing CI workflows and maintainer approval.
