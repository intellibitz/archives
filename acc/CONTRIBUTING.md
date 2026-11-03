# Contributing to AI Command Center (acc)

We love your input! We want to make acc the best zero-effort AI orchestration tool for everyone.

## How Can I Contribute?

### Reporting Bugs
- Use the GitHub Issue Tracker.
- Describe the steps to reproduce the bug.
- Include your hardware specs (GPU, RAM, OS).

### Suggesting Enhancements
- Open an issue with the tag `enhancement`.
- Describe the feature and why it fits the "Zero Effort" vision.

### ⚠️ Mandatory Rule for ACC Creators & Contributors
> **ALL Git and GitHub tasks MUST be executed using Gradle automation tasks ONLY.**
> All creators (AI agents, human developers, and contributors) must follow the automated Gradle workflow for branch creation, synchronization, pull request creation, auto-merging, and branch pruning. Direct manual `git` or `gh` commands are prohibited.

### Creator & Contributor Workflow
1. Fork and clone the repository.
2. Create a feature branch: `./gradlew githubFeature -Pname=MyFeature`
3. Implement your changes.
4. Sync and merge: `./gradlew githubMerge`
5. Sit back while CI passes, auto-merges the PR, and prunes merged remote/local branches automatically.

## Coding Standards
- **Shell**: Use `log` and `error` functions from `acc` for consistent output.
- **Kotlin**: Follow official Kotlin style guides. Use Koin for DI.
- **Documentation**: Keep the `README.md` clean and visually consistent.

## Vision Check
Every change should move the project closer to **Zero Effort**. If a feature makes the user type more or wait longer without feedback, it needs a redesign!

---
*Thank you for helping us build the future of local AI!*
