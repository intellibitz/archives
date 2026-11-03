GitHub Automation: safety flags and behavior

### ⚠️ Mandatory Creator Workflow Rule
> **STRICT RULE FOR ALL ACC CREATORS (you, me, everyone):**
> **All Git and GitHub tasks MUST be executed using Gradle automation tasks ONLY.**
> All creators (AI agents, human developers, and contributors) must follow the automated Gradle workflow for branch creation, synchronization, pull request creation, auto-merging, and branch pruning. Direct manual `git` or `gh` commands for repository collaboration are strictly prohibited.

This project includes a Gradle-based GitHub automation script (github-automation.gradle.kts) that performs common repository maintenance tasks.

New safety features

- confirm_or_abort helper: Requires explicit confirmation for destructive operations. When running interactively, the script will prompt for confirmation.

- MAIN_RESET_CONFIRM: When set to "true" (environment variable), allows the script to perform destructive resets of the default branch (e.g., git reset --hard). Without this, the script will abort when a reset would discard local changes or drop local commits.

- FORCE_PUSH_CONFIRM: When set to "true" (environment variable), permits force-with-lease pushes that overwrite a remote branch. Otherwise, the script refuses to force-push and will prompt for confirmation in interactive runs.

Usage examples

- Interactive local run (prompts):
  ./gradlew githubSync

- Non-interactive CI run (explicit confirmation via env):
  MAIN_RESET_CONFIRM=true FORCE_PUSH_CONFIRM=true ./gradlew githubSync

Notes

- The automation attempts to update all feature branches (gh pr update-branch) before enabling auto-merge.

- GitHub CLI (gh) authentication:
  - The script checks for 'gh' being installed and (when present) verifies 'gh auth status'. If 'gh' is installed but not authenticated, the script will abort unless GH_SKIP_AUTH_CHECK=true is set in the environment.
  - For interactive local runs, the script will prompt to proceed without authentication (not recommended).
  - Token lookup (first match wins): project properties (-P or gradle.properties) keys [GITHUB_TOKEN, githubToken, github.token, GH_TOKEN, ghToken]; env vars GITHUB_TOKEN / GH_TOKEN; then ~/.gradle/gradle.properties.
  - For CI, store the Personal Access Token (PAT) as a repository secret (recommended name: ACC_GITHUB_TOKEN or GITHUB_TOKEN) and inject it into the runner as GITHUB_TOKEN. Example step:

    env:
      GITHUB_TOKEN: ${{ secrets.ACC_GITHUB_TOKEN }}

    (Avoid committing gradle.properties containing secrets; use ~/.gradle/gradle.properties for local-only tokens and .gitignore prevents accidental commits.)
  - Alternatively, authenticate 'gh' in the runner with 'gh auth login --with-token' using the secret; do NOT print tokens to logs.

- CI safety recommendations:
  - Non-interactive runs must explicitly allow destructive actions by setting MAIN_RESET_CONFIRM=true and FORCE_PUSH_CONFIRM=true.
  - Prefer authenticating 'gh' in CI instead of bypassing the auth check.

- The docs live in docs/github-automation.md; update as needed when behavior changes.

CI workflow example

Below is an example GitHub Actions workflow (also committed to .github/workflows/github-automation.yml) that runs the preflight checks and optionally runs a Gradle automation task if provided via workflow_dispatch. Key points:

- The workflow runs ./gradlew githubPreflight first to verify gh authentication and the MAIN_RESET_CONFIRM/FORCE_PUSH_CONFIRM flags.
- For non-interactive CI runs set MAIN_RESET_CONFIRM and FORCE_PUSH_CONFIRM as repository secrets (or leave unset to prevent destructive changes).
- Prefer authenticating gh in CI using GITHUB_TOKEN rather than bypassing with GH_SKIP_AUTH_CHECK.

Snippet to include in other workflows (example step):

  - name: Run automation preflight
    run: ./gradlew githubPreflight
    env:
      MAIN_RESET_CONFIRM: ${{ secrets.MAIN_RESET_CONFIRM }}
      FORCE_PUSH_CONFIRM: ${{ secrets.FORCE_PUSH_CONFIRM }}
      GH_SKIP_AUTH_CHECK: ${{ secrets.GH_SKIP_AUTH_CHECK }}
      GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

Add this preflight step before any step that calls the actionable Gradle tasks (githubSync, githubMergeAll, githubMain, etc.).
E2E automation test at 20260902-203638

Rebase skip pattern

- REBASE_SKIP_PATTERN: Regex environment variable controlling which branches githubSync will skip rebasing. Default: '^(test/|automation/)'.
- Purpose: Prevent automated rebases for temporary or automation-managed branches (e.g., test/, automation/) so automation doesn't block on conflicts.
- Example (override): REBASE_SKIP_PATTERN='^(test/|automation/|wip/)' ./gradlew githubSync

Update the pattern in CI if you use different branch prefixes for temporary or automation branches.

Automation tasks summary

- github: High-level alias that runs githubSync, githubMergeAll, and githubStatus.
- githubStatus: Show repo, current/default branch, and PR status.
- githubSync: Safely update the default branch locally, capture local work, rebase current branch onto latest base (skips branches matching REBASE_SKIP_PATTERN), and push with lease. Guards against destructive resets and requires MAIN_RESET_CONFIRM/FORCE_PUSH_CONFIRM for risky operations.
- githubFeature: Create a feature branch from the default branch and open a PR.
- githubPR: Ensure the current branch has an open PR (creates one if missing).
- githubMerge: Enable auto-merge for the current branch PR (squash + delete branch on merge).
- githubMergeAll: Update all open PR branches and enable auto-merge for mergeable PRs.
- githubFixAll: Update PR branches, rerun failed CI runs, and enable auto-merge where appropriate.
- githubFixSecurity: Bulk-update and merge security-related PRs (Dependabot) when safe.
- githubMain: Reset local default branch to the remote state (requires confirmation and protects against data loss).
- githubCleanupClosedPRs / githubCleanupRemoteBranches: Scan merged/closed PRs and (dry-run by default) delete stale remote branches; enable actual deletion via DELETE_MODE/Gradle property.
- githubPruneLocalBranches: Prune local branches with no upstream, whose upstream was deleted, or merged into the base branch (dry-run by default; enable with PRUNE_MODE).
- githubPreflight: Pre-flight checks for gh authentication and effective flags (MAIN_RESET_CONFIRM, FORCE_PUSH_CONFIRM, GH_SKIP_AUTH_CHECK). Intended for CI and local safety.

AUTO_RESOLVE_MANUAL

- AUTO_RESOLVE_MANUAL: When set to 'true' (default), automation attempts to auto-resolve PRs that are waiting for manual intervention: it will update branches, rerun failing CI runs, and attempt to auto-approve PRs before enabling auto-merge. This is enabled by default to let Gradle automation merge PRs that only need manual approvals or CI reruns.
- To disable automated manual-resolution, set AUTO_RESOLVE_MANUAL=false in your environment or repository secrets.


- githubSetup: Convenience task to enable repo settings (auto-merge, delete-branch-on-merge, allow update-branch, enable squash merge).
- githubChecks: Show CI/checks status for the current PR/branch.
- githubPRSummary / githubSummary: Print a safe PR summary (open PRs, merged PRs, stale branches) and recommended actions.
- githubIssues, githubWiki, githubActions: Utility tasks to open/list issues, wiki, and actions UI.

Notes

- All destructive actions are guarded and default to dry-run unless enabled via explicit env flags or Gradle properties. The CI workflows default these flags to 'true' but can be overridden by repository secrets.
- Automation creates an audit artifact (automation-audit) with run details; destructive operations trigger an issue comment or creation labeled 'automation-alert'.



Auto-clean tasks

- githubCleanupRemoteBranches: Scans merged and closed PRs and (dry-run by default) deletes remote branches that are no longer needed. Enable actual deletion by setting the Gradle project property -PdeleteClosedPrBranches=true or environment variable DELETE_MODE=true in CI.

- githubPruneLocalBranches: Scans local branches and (dry-run by default) deletes local branches that have no upstream, whose upstream was deleted, or which were merged into the base branch. Enable actual pruning by setting -PpruneLocalBranches=true or environment variable PRUNE_MODE=true in CI. The task respects REBASE_SKIP_PATTERN and will not delete the current branch or the base branch.

Examples

# Dry-run remote cleanup
./gradlew githubCleanupRemoteBranches

# Actual remote deletion (CI safe via secret)
DELETE_MODE=true ./gradlew githubCleanupRemoteBranches

# Dry-run local prune
./gradlew githubPruneLocalBranches

# Actual local prune (use with care)
PRUNE_MODE=true ./gradlew githubPruneLocalBranches

Notes

- Both tasks default to dry-run and require explicit flags to mutate branches.
- Prefer running these tasks in CI with repository secrets that gate deletions.
- The scheduled automation workflow can be updated to call these tasks after githubMergeAll to keep branches tidy.
