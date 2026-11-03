import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.DefaultTask

// Implement tasks using 'git' and 'gh' CLIs for portability and to avoid external Java deps.

open class PRTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val branch = currentBranchInput
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        val repo = repoFull()
        if (branch.isBlank()) {
            logger.lifecycle("No current branch provided; skipping PR creation.")
            return
        }
        // Check existing PR for branch
        val (rc, out) = runCmd("gh", "pr", "list", "--head", branch, "--repo", repo, "--json", "number", "-q", ".[0].number")
        if (rc == 0 && out.isNotBlank()) {
            logger.lifecycle("PR already exists for ${branch}")
            return
        }
        val title = "Automated PR: ${branch}"
        val body = "Auto-created PR by automation"
        val (_, createOut) = runCmd("gh", "pr", "create", "--title", title, "--body", body, "--head", branch, "--base", base, "--repo", repo, "--assume-yes")
        logger.lifecycle("Created PR for ${branch} -> ${base}: ${createOut}")
    }
}

open class MergeTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val branch = currentBranchInput
        val repo = repoFull()
        if (branch.isBlank()) {
            logger.lifecycle("No current branch provided; skipping merge.")
            return
        }
        val (rc, prNum) = runCmd("gh", "pr", "list", "--head", branch, "--repo", repo, "--json", "number", "-q", ".[0].number")
        if (rc != 0 || prNum.isBlank()) {
            logger.lifecycle("No open PR found for ${branch}")
            return
        }
        val (mrc, mout) = runCmd("gh", "pr", "merge", prNum, "--squash", "--delete-branch", "--repo", repo, "--confirm")
        if (mrc == 0) logger.lifecycle("Merged PR #${prNum} for ${branch}") else logger.warn("Merge failed: ${mout}")
    }
}

open class MergeAllTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val repo = repoFull()
        val (rc, out) = runCmd("gh", "pr", "list", "--state", "open", "--repo", repo, "--json", "number,mergeable,mergeStateStatus", "-q", ".[] | [.number, .mergeable, .mergeStateStatus] | @tsv")
        if (rc != 0) { logger.warn("Failed to list PRs: ${out}"); return }
        out.lines().forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.split('\t')
            val pr = parts.getOrNull(0) ?: return@forEach
            val mergeable = parts.getOrNull(1) ?: ""
            val status = parts.getOrNull(2) ?: ""
            if (mergeable == "CONFLICTING" || mergeable == "DRAFT") {
                logger.lifecycle("Skipping PR #${pr}: conflicted or draft")
                return@forEach
            }
            logger.lifecycle("Attempting to merge PR #${pr} (status=${status})")
            val (mrc, mout) = runCmd("gh", "pr", "merge", pr, "--auto", "--squash", "--delete-branch", "--repo", repo)
            if (mrc != 0) logger.warn("Merge/enable auto-merge failed for #${pr}: ${mout}") else logger.lifecycle("Enabled merge for #${pr}")
        }
    }
}

open class SyncTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val repoDir = rootDir
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        runCmd("git", "fetch", "origin", workingDir = repoDir)
        val (rc, out) = runCmd("git", "checkout", base, workingDir = repoDir)
        if (rc != 0) { logger.warn("Checkout failed: ${out}"); return }
        val (rc2, out2) = runCmd("git", "pull", "origin", base, workingDir = repoDir)
        if (rc2 == 0) logger.lifecycle("Updated local ${base} from origin/${base}") else logger.warn("Pull failed: ${out2}")

        // Automatically prune local branches merged into base after sync
        runCmd("git", "fetch", "origin", "--prune", workingDir = repoDir)
        val (bRc, bOut) = runCmd("git", "for-each-ref", "--format=%(refname:short)", "refs/heads/", workingDir = repoDir)
        if (bRc == 0) {
            bOut.lines().forEach { branch ->
                val b = branch.trim()
                if (b.isBlank() || b == base || b == "main" || b == "master") return@forEach
                val (mRc, _) = runCmd("git", "merge-base", "--is-ancestor", b, "origin/$base", workingDir = repoDir)
                if (mRc == 0) {
                    logger.lifecycle("Auto-pruning merged local branch '$b'")
                    runCmd("git", "branch", "-D", b, workingDir = repoDir)
                }
            }
        }
    }
}

open class CleanupRemoteBranchesTask : BaseGitHubTask() {
    @get:Input
    val deleteModeInput: Boolean = (project.findProperty("DELETE_MODE") as? String)?.toBoolean()
        ?: (project.findProperty("deleteClosedPrBranches") as? String)?.toBoolean()
        ?: (System.getenv("DELETE_MODE")?.toBoolean() ?: true)

    @TaskAction
    fun run() {
        val repo = repoFull()
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        val current = currentBranchInput
        val repoDir = rootDir

        logger.lifecycle("Cleaning up merged/stale remote branches for repository $repo (deleteMode=$deleteModeInput)...")
        runCmd("git", "fetch", "origin", "--prune", workingDir = repoDir)

        val deleted = mutableSetOf<String>()

        // 1. Check head branches from merged and closed PRs
        for (state in listOf("merged", "closed")) {
            val (rc, out) = runCmd("gh", "pr", "list", "--state", state, "--repo", repo, "--limit", "1000", "--json", "number,headRefName", "-q", ".[] | [.number, .headRefName] | @tsv")
            if (rc == 0) {
                out.lines().forEach { line ->
                    if (line.isBlank()) return@forEach
                    val parts = line.split('\t')
                    val pr = parts.getOrNull(0) ?: return@forEach
                    val branch = parts.getOrNull(1) ?: return@forEach
                    if (branch.isBlank() || branch == base || branch == current || branch == "main" || branch == "master") return@forEach
                    if (deleted.contains(branch)) return@forEach

                    logger.lifecycle("PR #$pr ($state) branch '$branch' detected for remote cleanup.")
                    if (deleteModeInput) {
                        val (dRc, dOut) = runCmd("gh", "api", "-X", "DELETE", "repos/$repo/git/refs/heads/$branch")
                        if (dRc == 0) {
                            logger.lifecycle("Successfully deleted remote branch '$branch'")
                            deleted.add(branch)
                        } else {
                            logger.lifecycle("Remote branch '$branch' already deleted or protected: $dOut")
                        }
                    } else {
                        logger.lifecycle("[DRY-RUN] Would delete remote branch '$branch'")
                    }
                }
            }
        }

        // 2. Check all remote branches to see if they are merged into origin/base or have no open PR
        val (rRc, rOut) = runCmd("git", "for-each-ref", "--format=%(refname:short)", "refs/remotes/origin/", workingDir = repoDir)
        if (rRc == 0) {
            rOut.lines().forEach { ref ->
                val branch = ref.removePrefix("origin/").trim()
                if (branch.isBlank() || branch == "HEAD" || branch == base || branch == current || branch == "main" || branch == "master") return@forEach
                if (deleted.contains(branch)) return@forEach

                val (mRc, _) = runCmd("git", "merge-base", "--is-ancestor", "origin/$branch", "origin/$base", workingDir = repoDir)
                val isMerged = mRc == 0

                val (pRc, pOut) = runCmd("gh", "pr", "list", "--head", branch, "--repo", repo, "--state", "open", "--json", "number", "-q", ".[0].number")
                val hasOpenPr = pRc == 0 && pOut.isNotBlank()

                if (isMerged || (!hasOpenPr && (branch.startsWith("sync/") || branch.startsWith("automation/") || branch.startsWith("feature/")))) {
                    val reason = if (isMerged) "merged into $base" else "stale branch with no open PR"
                    logger.lifecycle("Remote branch 'origin/$branch' is eligible for deletion ($reason).")
                    if (deleteModeInput) {
                        val (dRc, dOut) = runCmd("gh", "api", "-X", "DELETE", "repos/$repo/git/refs/heads/$branch")
                        if (dRc == 0) {
                            logger.lifecycle("Successfully deleted remote branch '$branch'")
                            deleted.add(branch)
                        } else {
                            logger.lifecycle("Remote branch '$branch' already deleted or protected: $dOut")
                        }
                    } else {
                        logger.lifecycle("[DRY-RUN] Would delete remote branch '$branch'")
                    }
                }
            }
        }
    }
}

open class PruneLocalBranchesTask : BaseGitHubTask() {
    @get:Input
    val pruneModeInput: Boolean = (project.findProperty("PRUNE_MODE") as? String)?.toBoolean()
        ?: (project.findProperty("pruneLocalBranches") as? String)?.toBoolean()
        ?: (System.getenv("PRUNE_MODE")?.toBoolean() ?: true)

    @TaskAction
    fun run() {
        val repoDir = rootDir
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        val (cRc, cOut) = runCmd("git", "rev-parse", "--abbrev-ref", "HEAD", workingDir = repoDir)
        val currentBranch = if (cRc == 0) cOut.trim() else ""

        logger.lifecycle("Pruning local merged/stale branches (pruneMode=$pruneModeInput)...")
        runCmd("git", "fetch", "origin", "--prune", workingDir = repoDir)

        val (rc, out) = runCmd("git", "for-each-ref", "--format=%(refname:short)", "refs/heads/", workingDir = repoDir)
        if (rc != 0) {
            logger.warn("Failed to enumerate local branches: $out")
            return
        }

        out.lines().forEach { branch ->
            val b = branch.trim()
            if (b.isBlank() || b == base || b == currentBranch || b == "main" || b == "master") return@forEach

            val (mRc, _) = runCmd("git", "merge-base", "--is-ancestor", b, "origin/$base", workingDir = repoDir)
            val isMerged = mRc == 0

            val (rRc, rOut) = runCmd("git", "ls-remote", "--exit-code", "--heads", "origin", b, workingDir = repoDir)
            val remoteMissing = rRc != 0 || rOut.isBlank()

            if (isMerged || remoteMissing) {
                val reason = if (isMerged) "merged into $base" else "remote branch missing on origin"
                logger.lifecycle("Local branch '$b' is eligible for deletion ($reason).")
                if (pruneModeInput) {
                    val (dRc, dOut) = runCmd("git", "branch", "-D", b, workingDir = repoDir)
                    if (dRc == 0) {
                        logger.lifecycle("Successfully deleted local branch '$b'")
                    } else {
                        logger.warn("Failed to delete local branch '$b': $dOut")
                    }
                } else {
                    logger.lifecycle("[DRY-RUN] Would delete local branch '$b'")
                }
            }
        }
    }
}

open class CheckAndCleanPRTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val repo = repoFull()
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        val (cRc, cOut) = runCmd("git", "rev-parse", "--abbrev-ref", "HEAD", workingDir = rootDir)
        val activeBranch = if (cRc == 0) cOut.trim() else ""
        val targetBranch = currentBranchInput.ifBlank { activeBranch }

        logger.lifecycle("Checking PR status and branch cleanup for '$targetBranch' on $repo...")

        if (targetBranch.isBlank() || targetBranch == base || targetBranch == "main" || targetBranch == "master") {
            logger.lifecycle("Target branch is default/main; checking all merged PR branches across $repo...")
            val (rc, out) = runCmd("gh", "pr", "list", "--state", "merged", "--repo", repo, "--limit", "500", "--json", "number,headRefName", "-q", ".[] | [.number, .headRefName] | @tsv")
            if (rc == 0) {
                val deleted = mutableSetOf<String>()
                out.lines().forEach { line ->
                    if (line.isBlank()) return@forEach
                    val parts = line.split('\t')
                    val pr = parts.getOrNull(0) ?: return@forEach
                    val head = parts.getOrNull(1) ?: return@forEach
                    if (head.isBlank() || head == base || head == "main" || head == "master") return@forEach
                    if (deleted.contains(head)) return@forEach

                    logger.lifecycle("PR #$pr for branch '$head' is MERGED. Deleting remote and local branch...")
                    runCmd("gh", "api", "-X", "DELETE", "repos/$repo/git/refs/heads/$head")
                    runCmd("git", "branch", "-D", head, workingDir = rootDir)
                    deleted.add(head)
                }
            }
            return
        }

        // Check PR for target branch
        val (rc, out) = runCmd("gh", "pr", "view", targetBranch, "--repo", repo, "--json", "number,state,mergedAt,title,url")
        if (rc != 0 || out.isBlank()) {
            logger.lifecycle("No PR found for branch '$targetBranch'.")
            return
        }

        logger.lifecycle("PR Info: $out")
        val isMerged = out.contains("\"state\":\"MERGED\"") || (out.contains("\"mergedAt\":") && !out.contains("\"mergedAt\":null"))

        if (isMerged) {
            logger.lifecycle("PR for branch '$targetBranch' is MERGED! Deleting remote and local branch...")

            // Return to main branch if currently on the merged branch
            if (activeBranch == targetBranch) {
                logger.lifecycle("Switching from merged branch '$targetBranch' back to '$base'...")
                runCmd("git", "checkout", base, workingDir = rootDir)
                runCmd("git", "pull", "origin", base, workingDir = rootDir)
            }

            // Delete remote branch
            val (dRc, dOut) = runCmd("gh", "api", "-X", "DELETE", "repos/$repo/git/refs/heads/$targetBranch")
            if (dRc == 0) logger.lifecycle("Deleted remote branch 'origin/$targetBranch'") else logger.lifecycle("Remote branch 'origin/$targetBranch' status: $dOut")

            // Delete local branch
            val (lRc, lOut) = runCmd("git", "branch", "-D", targetBranch, workingDir = rootDir)
            if (lRc == 0) logger.lifecycle("Deleted local branch '$targetBranch'") else logger.lifecycle("Local branch '$targetBranch' status: $lOut")
        } else {
            logger.lifecycle("PR for branch '$targetBranch' exists but is not yet merged.")
        }
    }
}

open class FixAllTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val repo = repoFull()
        val (rc, out) = runCmd("gh", "pr", "list", "--state", "open", "--repo", repo, "--json", "number,mergeable,mergeStateStatus", "-q", ".[] | [.number, .mergeable, .mergeStateStatus] | @tsv")
        if (rc != 0) { logger.warn("Failed to list PRs: ${out}"); return }
        out.lines().forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.split('\t')
            val pr = parts.getOrNull(0) ?: return@forEach
            val mergeable = parts.getOrNull(1) ?: ""
            if (mergeable == "MERGEABLE") {
                val (mrc, mout) = runCmd("gh", "pr", "merge", pr, "--squash", "--delete-branch", "--repo", repo)
                if (mrc != 0) logger.warn("Failed to merge #${pr}: ${mout}") else logger.lifecycle("Merged #${pr}")
            }
        }
    }
}

open class PRSummaryTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val repo = repoFull()
        logger.lifecycle("Open PRs:")
        val (rc, open) = runCmd("gh", "pr", "list", "--state", "open", "--repo", repo, "--json", "number,title,headRefName,mergeable,mergeStateStatus", "-q", ".[] | [.number, .title, .headRefName, .mergeable, .mergeStateStatus] | @tsv")
        if (rc == 0) {
            open.lines().forEach { logger.lifecycle(it) }
        } else logger.warn("Failed to list open PRs: ${open}")
        logger.lifecycle("\nMerged PRs (recent):")
        val (rc2, merged) = runCmd("gh", "pr", "list", "--state", "merged", "--repo", repo, "--json", "number,title,headRefName", "-q", ".[] | [.number, .title, .headRefName] | @tsv")
        if (rc2 == 0) merged.lines().forEach { logger.lifecycle(it) } else logger.warn("Failed to list merged PRs: ${merged}")
    }
}

open class FixSecurityTask : BaseGitHubTask() {
    @TaskAction
    fun run() {
        val repo = repoFull()
        logger.lifecycle("Looking for Dependabot/security PRs to merge...")
        val (rc, out) = runCmd("gh", "pr", "list", "--state", "open", "--repo", repo, "--json", "number,title", "-q", ".[] | [.number, .title] | @tsv")
        if (rc != 0) { logger.warn("Failed to list PRs: ${out}"); return }
        out.lines().forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.split('\t')
            val pr = parts.getOrNull(0) ?: return@forEach
            val title = parts.getOrNull(1) ?: ""
            val t = title.lowercase()
            if (t.contains("dependabot") || t.contains("security")) {
                val (mrc, mout) = runCmd("gh", "pr", "merge", pr, "--auto", "--squash", "--delete-branch", "--repo", repo)
                if (mrc != 0) logger.warn("Failed to merge security PR #${pr}: ${mout}") else logger.lifecycle("Merged security PR #${pr}: ${title}")
            }
        }
    }
}

open class SimpleTaskLogger : DefaultTask() {
    @TaskAction
    fun run() {
        logger.lifecycle("This is a placeholder task for simple actions (issues/wiki/checks).")
    }
}


open class FeatureTask : BaseGitHubTask() {
    @get:Input
    val featureNameInput: String = (project.findProperty("featureName") as? String) ?: (project.findProperty("name") as? String) ?: ""

    @TaskAction
    fun run() {
        val repoDir = rootDir
        val nameToUse = if (featureNameInput.isNotBlank()) featureNameInput else "feature-${System.currentTimeMillis()}"
        val safe = nameToUse.replace(Regex("[^A-Za-z0-9._/-]"), "-").trim('-')
        val branch = "feature/${safe}"
        val (rc, out) = runCmd("git", "checkout", "-b", branch, workingDir = repoDir)
        if (rc != 0) { logger.warn("Failed to create branch: ${out}"); return }
        val (pRc, pOut) = runCmd("git", "push", "-u", "origin", branch, workingDir = repoDir)
        if (pRc == 0) logger.lifecycle("Created and pushed branch ${branch}") else logger.warn("Failed to push branch: ${pOut}")
    }
}

// MainTask is implemented below
open class MainTask : DefaultTask() {
    @get:Internal
    val rootDir: File = project.rootDir

    @get:Input
    val baseBranchInput: String = (project.findProperty("BASE_BRANCH") as? String) ?: System.getenv("BASE_BRANCH") ?: "main"

    @TaskAction
    fun run() {
        val repoDir = rootDir
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        runCmd("git", "checkout", base, workingDir = repoDir)
        runCmd("git", "fetch", "origin", workingDir = repoDir)
        val (rc2, out2) = runCmd("git", "reset", "--hard", "origin/${base}", workingDir = repoDir)
        if (rc2 == 0) logger.lifecycle("Reset local ${base} to origin/${base}") else logger.warn("Failed to reset main: ${out2}")
    }
}
