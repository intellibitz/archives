import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.DefaultTask

// Lightweight helpers using 'git' and 'gh' CLIs where available.
fun runCmd(vararg cmd: String, workingDir: File? = null, env: Map<String, String> = emptyMap()): Pair<Int, String> {
    val pb = ProcessBuilder(*cmd)
    if (workingDir != null) pb.directory(workingDir)
    val e = pb.environment()
    e.putAll(env)
    pb.redirectErrorStream(true)
    val proc = pb.start()
    val out = proc.inputStream.bufferedReader().readText()
    val rc = proc.waitFor()
    return rc to out.trim()
}

fun inferRepoFromGit(root: File): String? {
    val (rc, out) = runCmd("git", "remote", "get-url", "origin", workingDir = root)
    if (rc != 0 || out.isBlank()) return null
    var cleaned = out
        .removePrefix("git@github.com:")
        .removePrefix("https://github.com/")
        .removeSuffix(".git")
    cleaned = cleaned.trim()
    return cleaned
}

open class AutomationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("githubPreflightKotlin", PreflightTask::class.java)
        project.tasks.register("githubSetupKotlin", SetupTask::class.java)
        project.tasks.register("githubRemoveObsoleteBranchesKotlin", RemoveRemoteObsoleteBranchesTask::class.java)
        project.tasks.register("githubRemoveLocalObsoleteBranchesKotlin", RemoveLocalObsoleteBranchesTask::class.java)
        project.tasks.register("githubPRKotlin", PRTask::class.java)
        project.tasks.register("githubMergeKotlin", MergeTask::class.java)
        project.tasks.register("githubMergeAllKotlin", MergeAllTask::class.java)
        project.tasks.register("githubSyncKotlin", SyncTask::class.java)
        project.tasks.register("githubFeatureKotlin", FeatureTask::class.java)
        project.tasks.register("githubMainKotlin", MainTask::class.java)
        project.tasks.register("githubCleanupRemoteBranchesKotlin", CleanupRemoteBranchesTask::class.java)
        project.tasks.register("githubPruneLocalBranchesKotlin", PruneLocalBranchesTask::class.java)
        project.tasks.register("githubCheckAndCleanPRBranchKotlin", CheckAndCleanPRTask::class.java)
        project.tasks.register("githubFixAllKotlin", FixAllTask::class.java)
        project.tasks.register("githubPRSummaryKotlin", PRSummaryTask::class.java)
        project.tasks.register("githubFixSecurityKotlin", FixSecurityTask::class.java)
        project.tasks.register("githubChecksKotlin", SimpleTaskLogger::class.java)
        project.tasks.register("githubIssuesKotlin", SimpleTaskLogger::class.java)
        project.tasks.register("githubWikiKotlin", SimpleTaskLogger::class.java)
    }
}

abstract class BaseGitHubTask : DefaultTask() {
    @get:Internal
    val rootDir: File = project.rootDir

    @get:Input
    val githubTokenInput: String = resolveToken(project)

    @get:Input
    val githubRepositoryInput: String = (project.findProperty("GITHUB_REPOSITORY") as? String) ?: System.getenv("GITHUB_REPOSITORY") ?: ""

    @get:Input
    val currentBranchInput: String = (project.findProperty("CURRENT_BRANCH") as? String) ?: System.getenv("CURRENT_BRANCH") ?: ""

    @get:Input
    val baseBranchInput: String = (project.findProperty("BASE_BRANCH") as? String) ?: System.getenv("BASE_BRANCH") ?: "main"

    fun token(): String {
        if (githubTokenInput.isNotBlank()) return githubTokenInput
        throw IllegalStateException("GITHUB_TOKEN is required for Kotlin-native automation tasks. Provide via gradle properties or env.")
    }

    fun repoFull(): String {
        if (githubRepositoryInput.isNotBlank()) return githubRepositoryInput
        val inferred = inferRepoFromGit(rootDir)
        if (!inferred.isNullOrBlank()) return inferred
        throw IllegalStateException("Could not determine repository owner/name. Set -PGITHUB_REPOSITORY or ensure 'git' remote origin is configured.")
    }

    companion object {
        fun resolveToken(project: Project): String {
            val propKeys = listOf("GITHUB_TOKEN", "githubToken", "github.token", "GH_TOKEN", "ghToken")
            for (k in propKeys) {
                val v = project.findProperty(k) as? String
                if (!v.isNullOrBlank()) return v.trim()
            }
            val env = System.getenv()
            val envTok = env["GITHUB_TOKEN"] ?: env["GH_TOKEN"] ?: env["github_token"]
            if (!envTok.isNullOrBlank()) return envTok.trim()
            val home = System.getProperty("user.home") ?: ""
            try {
                val f = java.io.File(home, ".gradle/gradle.properties")
                if (f.exists()) {
                    val props = java.util.Properties()
                    f.inputStream().use { props.load(it) }
                    for (k in propKeys) {
                        val v = props.getProperty(k)
                        if (!v.isNullOrBlank()) return v.trim()
                    }
                }
            } catch (_: Exception) {}
            return ""
        }
    }
}

open class PreflightTask : BaseGitHubTask() {
    @TaskAction
    fun runPreflight() {
        logger.lifecycle("Running Kotlin preflight checks...")
        try {
            val t = token()
            logger.lifecycle("Token available (masked). Will validate gh auth if available.")
            val (rc, out) = runCmd("gh", "auth", "status")
            if (rc == 0) {
                logger.lifecycle("gh CLI authenticated: $out")
            } else {
                logger.lifecycle("gh CLI not authenticated or not present: $out")
            }
        } catch (e: Exception) {
            logger.warn("Preflight failed: ${e.message}")
            throw e
        }
    }
}

open class SetupTask : BaseGitHubTask() {
    @TaskAction
    fun runSetup() {
        val repo = repoFull()
        logger.lifecycle("Updating repository settings via gh...")
        val (rc, out) = runCmd("gh", "repo", "edit", repo, "--enable-auto-merge", "--delete-branch-on-merge", "--allow-update-branch", "--enable-squash-merge")
        if (rc != 0) logger.warn("gh repo edit failed: $out") else logger.lifecycle("Repository settings updated.")
    }
}

open class RemoveRemoteObsoleteBranchesTask : BaseGitHubTask() {
    @get:Input
    val removeModeInput: Boolean = (project.findProperty("REMOVE_MODE") as? String)?.toBoolean()
        ?: (System.getenv("REMOVE_MODE")?.toBoolean() ?: true)

    @get:Input
    val removeDaysInput: Long = (project.findProperty("REMOVE_DAYS") as? String)?.toLong()
        ?: (System.getenv("REMOVE_DAYS")?.toLong() ?: 90L)

    @TaskAction
    fun run() {
        val repo = repoFull()
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        val current = currentBranchInput
        logger.lifecycle("Scanning and removing merged/obsolete remote branches for $repo...")

        val (rc, out) = runCmd("gh", "pr", "list", "--state", "merged", "--repo", repo, "--limit", "500", "--json", "number,headRefName", "-q", ".[] | [.number, .headRefName] | @tsv")
        if (rc != 0) { logger.warn("Failed to list merged PRs: $out"); return }

        val deleted = mutableSetOf<String>()
        out.lines().forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.split('\t')
            val branch = parts.getOrNull(1) ?: return@forEach
            if (branch.isBlank() || branch == base || branch == current || branch == "main" || branch == "master") return@forEach
            if (deleted.contains(branch)) return@forEach

            logger.lifecycle("Merged branch '$branch' eligible for remote deletion.")
            if (removeModeInput) {
                val (dRc, dOut) = runCmd("gh", "api", "-X", "DELETE", "repos/$repo/git/refs/heads/$branch")
                if (dRc == 0) {
                    logger.lifecycle("Deleted remote branch '$branch'")
                    deleted.add(branch)
                } else {
                    logger.lifecycle("Remote branch '$branch' deleted or protected: $dOut")
                }
            } else {
                logger.lifecycle("[DRY-RUN] Would delete remote branch '$branch'")
            }
        }
    }
}

open class RemoveLocalObsoleteBranchesTask : BaseGitHubTask() {
    @get:Input
    val pruneModeInput: Boolean = (project.findProperty("PRUNE_LOCAL_MODE") as? String)?.toBoolean()
        ?: (System.getenv("PRUNE_LOCAL_MODE")?.toBoolean() ?: true)

    @get:Input
    val pruneDaysInput: Long = (project.findProperty("PRUNE_LOCAL_DAYS") as? String)?.toLong()
        ?: (System.getenv("PRUNE_LOCAL_DAYS")?.toLong() ?: 90L)

    @TaskAction
    fun run() {
        val repoDir = rootDir
        val base = if (baseBranchInput.isNotBlank()) baseBranchInput else "main"
        val (cRc, cOut) = runCmd("git", "rev-parse", "--abbrev-ref", "HEAD", workingDir = repoDir)
        val current = if (cRc == 0) cOut.trim() else ""

        runCmd("git", "fetch", "origin", "--prune", workingDir = repoDir)
        val (rc, out) = runCmd("git", "for-each-ref", "--format=%(refname:short)", "refs/heads/", workingDir = repoDir)
        if (rc != 0) { logger.warn("Failed to enumerate local branches: $out"); return }

        out.lines().forEach { branch ->
            val b = branch.trim()
            if (b.isBlank() || b == base || b == current || b == "main" || b == "master") return@forEach

            val (mRc, _) = runCmd("git", "merge-base", "--is-ancestor", b, "origin/$base", workingDir = repoDir)
            val isMerged = mRc == 0

            val (rRc, rOut) = runCmd("git", "ls-remote", "--exit-code", "--heads", "origin", b, workingDir = repoDir)
            val remoteMissing = rRc != 0 || rOut.isBlank()

            if (isMerged || remoteMissing) {
                logger.lifecycle("Local branch '$b' eligible for deletion (merged=$isMerged, remoteMissing=$remoteMissing).")
                if (pruneModeInput) {
                    val (dRc, dOut) = runCmd("git", "branch", "-D", b, workingDir = repoDir)
                    if (dRc == 0) logger.lifecycle("Deleted local branch '$b'") else logger.warn("Failed to delete '$b': $dOut")
                } else {
                    logger.lifecycle("[DRY-RUN] Would delete local branch '$b'")
                }
            }
        }
    }
}
