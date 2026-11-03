/**
 * GitHub Automation Tasks (100% Pure Kotlin / Platform-Independent)
 *
 * All tasks delegate to Kotlin-native task implementations provided by AutomationPlugin.
 */

tasks.register("github") {
    group = "github"
    description = "Runs the creator workflow: sync latest main, update current branch, merge clean PRs, prune merged remote & local branches, and report state."
    dependsOn("githubSyncKotlin", "githubMergeAllKotlin", "githubCleanupRemoteBranchesKotlin", "githubPruneLocalBranchesKotlin", "githubPRSummaryKotlin")
}

tasks.register("githubSync") {
    group = "github"
    description = "Smart sync (Kotlin-native): updates main, rebases current branch, and prunes local merged branches."
    dependsOn("githubSyncKotlin", "githubPruneLocalBranchesKotlin")
}

tasks.register("githubOpen") {
    group = "github"
    description = "Open repository web view (Kotlin-native)"
    dependsOn("githubSetupKotlin")
}

tasks.register("githubFeature") {
    group = "github"
    description = "Create feature branch (Kotlin-native)"
    dependsOn("githubSyncKotlin", "githubFeatureKotlin")
}

tasks.register("githubMain") {
    group = "github"
    description = "Return repo to default branch (Kotlin-native)"
    dependsOn("githubMainKotlin")
}

tasks.register("githubCleanupRemoteBranches") {
    group = "github"
    description = "Cleanup remote branches for merged PRs (Kotlin-native)"
    dependsOn("githubCleanupRemoteBranchesKotlin")
}

tasks.register("githubPruneLocalBranches") {
    group = "github"
    description = "Prune local branches merged into main or missing remote (Kotlin-native)"
    dependsOn("githubPruneLocalBranchesKotlin")
}

tasks.register("githubCheckAndCleanPRBranch") {
    group = "github"
    description = "Checks if PR exists for branch; if merged, deletes both remote and local PR branches (Kotlin-native)"
    dependsOn("githubCheckAndCleanPRBranchKotlin")
}

tasks.register("githubPR") {
    group = "github"
    description = "Ensure PR exists (Kotlin-native)"
    dependsOn("githubSyncKotlin", "githubPRKotlin")
}

tasks.register("githubMerge") {
    group = "github"
    description = "Merge current branch PR and cleanup branches (Kotlin-native)"
    dependsOn("githubPRKotlin", "githubMergeKotlin", "githubCleanupRemoteBranchesKotlin", "githubPruneLocalBranchesKotlin")
}

tasks.register("githubMergeAll") {
    group = "github"
    description = "Updates, auto-merges all open pull requests, and cleans up remote/local merged branches (Kotlin-native)"
    dependsOn("githubSyncKotlin", "githubMergeAllKotlin", "githubCleanupRemoteBranchesKotlin", "githubPruneLocalBranchesKotlin")
}

tasks.register("githubCleanupClosedPRs") {
    group = "github"
    description = "Lists merged/closed PRs and deletes stale remote branch refs (Kotlin-native)"
    dependsOn("githubCleanupRemoteBranchesKotlin")
}

tasks.register("githubPRSummary") {
    group = "github"
    description = "Prints a safe PR summary (Kotlin-native)"
    dependsOn("githubPRSummaryKotlin")
}

tasks.register("githubSummary") {
    group = "github"
    description = "Alias for githubPRSummary."
    dependsOn("githubPRSummaryKotlin")
}

tasks.register("githubStatus") {
    group = "github"
    description = "Alias for githubPRSummary."
    dependsOn("githubPRSummaryKotlin")
}

tasks.register("githubFixAll") {
    group = "github"
    description = "Attempts to fix all open PRs by updating them and rerunning failed workflow runs (Kotlin-native)"
    dependsOn("githubSyncKotlin", "githubFixAllKotlin")
}

tasks.register("githubFixSecurity") {
    group = "github"
    description = "Checks repository security alerts and merges any security-related PRs when appropriate (Kotlin-native)."
    dependsOn("githubSyncKotlin", "githubFixSecurityKotlin")
}

tasks.register("githubSetup") {
    group = "github"
    description = "Optimizes the repository settings for GitHub automation workflows (Kotlin-native)."
    dependsOn("githubSetupKotlin")
}

tasks.register("githubPreflight") {
    group = "github"
    description = "Performs pre-flight checks for automation (Kotlin-native)."
    dependsOn("githubPreflightKotlin")
}

tasks.register("githubChecks") {
    group = "github"
    description = "Displays the status of GitHub Action checks for the current branch (Kotlin-native)."
    dependsOn("githubChecksKotlin")
}

tasks.register("githubIssues") {
    group = "github"
    description = "Lists all open issues for the project (Kotlin-native)."
    dependsOn("githubIssuesKotlin")
}

tasks.register("githubWiki") {
    group = "github"
    description = "Open the project Wiki (Kotlin-native)."
    dependsOn("githubWikiKotlin")
}

tasks.register("githubRemoveObsoleteBranches") {
    group = "github"
    description = "Remove obsolete remote branches (Kotlin-native)"
    dependsOn("githubRemoveObsoleteBranchesKotlin")
}

tasks.register("githubRemoveLocalObsoleteBranches") {
    group = "github"
    description = "Prune local obsolete branches (Kotlin-native)"
    dependsOn("githubRemoveLocalObsoleteBranchesKotlin")
}

tasks.register("githubActions") {
    group = "github"
    description = "Open GitHub Actions (Kotlin-native)"
    dependsOn("githubChecksKotlin")
}
