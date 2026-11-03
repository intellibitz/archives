Automation Guide — ACC Repository

### ⚠️ Mandatory Creator Workflow Rule
> **STRICT RULE FOR ALL ACC CREATORS (you, me, everyone):**
> **All Git and GitHub tasks MUST be executed using Gradle automation tasks ONLY.**
> All creators (AI agents, human developers, and contributors) must follow the automated Gradle workflow for branch creation, synchronization, pull request creation, auto-merging, and branch pruning. Direct manual `git` or `gh` commands for repository collaboration are strictly prohibited.

This document describes the repository automation implemented via Gradle tasks and GitHub Actions. It covers available tasks, workflows, secrets, safe usage, and quick commands for dry-run vs live operations.

Overview

- Automation is implemented in github-automation.gradle.kts and driven by workflows in .github/workflows/*.yml.
- Key goals: auto-update branches, auto-merge CI-green PRs, remove obsolete branches (remote/local), and auto-promote test prereleases to production.

Important Workflows

- .github/workflows/github-automation.yml
  - Trigger: workflow_dispatch or push to main
  - Runs preflight checks and an optional gradle task input (default: githubMergeAll)
  - Optional local prune step (runs when secret PRUNE_LOCAL_BRANCHES=true)

- .github/workflows/github-automation-scheduled.yml
  - Trigger: daily CRON (default 02:00 UTC), workflow_dispatch, or tag pushes
  - Runs githubFixAll, githubMergeAll, githubRemoveObsoleteBranchesKotlin (Kotlin-native), and (optionally) local prune (githubRemoveLocalObsoleteBranchesKotlin)

- .github/workflows/auto-promote-release.yml
  - Trigger: release published
  - Promotes tags matching -test.* to production tags

Key Gradle Tasks

- githubPreflight
  - Validates GH auth and required env flags. Treats GITHUB_TOKEN as authenticated in CI.

- githubSync
  - Smart sync/rebase/push for the current branch

- githubPR / githubMerge / githubMergeAll / githubFixAll
  - PR creation, enabling auto-merge, and bulk fixes/merges

- githubCleanupRemoteBranches
  - Safe remote cleanup (dry-run by default). Controlled by DELETE_MODE or -PdeleteClosedPrBranches

- githubRemoveObsoleteBranchesKotlin
  - New Kotlin-native task: removes remote branches that are merged or inactive older than a cutoff
  - Dry-run by default. Enable live deletes with REMOVE_MODE=true or -PremoveObsoleteBranches=true
  - Cutoff defaults to 90 days (set with REMOVE_DAYS)

- githubRemoveLocalObsoleteBranchesKotlin
  - New Kotlin-native task: prunes local branches that are merged into base, have no upstream, or are older than cutoff
  - Dry-run by default. Enable with PRUNE_LOCAL_MODE=true or -PpruneLocalBranches=true
  - Note: running on GitHub-hosted runners only affects that ephemeral runner; enable on self-hosted runners only when desired

Release Automation

- release.yml builds artifacts and creates GitHub Releases for tag pushes (v*). The auto-promote workflow creates production tags for -test.* prereleases.

Secrets and Flags

- MAIN_RESET_CONFIRM, FORCE_PUSH_CONFIRM: safety confirmations for destructive git resets/pushes
- GH_SKIP_AUTH_CHECK: bypass gh auth check (not recommended)
- AUTO_RESOLVE_MANUAL: when true, automation auto-confirms manual prompts in non-interactive runs
- PRUNE_LOCAL_BRANCHES (secret): when set to 'true' the workflows will run local pruning steps
- PRUNE_LOCAL_DAYS (secret): cutoff in days for local pruning (default 90)
- REMOVE_MODE / REMOVE_DAYS: control remote obsolete branch deletion and cutoff
- DELETE_REMOTE_BRANCHES: controls older cleanup task

Common Commands

- Dry-run remote obsolete removal:
  ./gradlew githubRemoveObsoleteBranches

- Live remote deletion:
  REMOVE_MODE=true ./gradlew githubRemoveObsoleteBranches -PremoveObsoleteBranches=true

- Dry-run local prune:
  ./gradlew githubRemoveLocalObsoleteBranches

- Live local prune (runner/local machine):
  PRUNE_LOCAL_MODE=true PRUNE_LOCAL_DAYS=90 ./gradlew githubRemoveLocalObsoleteBranches -PpruneLocalBranches=true

- Run preflight locally:
  ./gradlew githubPreflight

Admin Notes & Safety

- By default destructive tasks run in dry-run mode. Enable deletion flags explicitly and store secrets when enabling on CI.
- Local pruning on CI affects only the runner; use PRUNE_LOCAL_BRANCHES=true only on self-hosted runners or when intentional.
- All automation logs are uploaded as artifacts (automation-audit) and destructive actions create/append an automation-alert issue when detected.

Troubleshooting

- If gh is installed but unauthenticated, set GITHUB_TOKEN or GH_SKIP_AUTH_CHECK (less secure).
- To change retention/cutoff, set REMOVE_DAYS or PRUNE_LOCAL_DAYS via repository secrets.

Contact & Rollback

- Rollback: if automation deleted a branch, it can usually be restored from the merged commit (re-create tag or branch from commit SHA). Check automation-audit artifact for details.
- For questions or to change policy, open an issue labeled automation-alert or contact repo admins.
