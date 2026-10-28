package icu.windea.pls.base.io

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProgressExecutor
import icu.windea.pls.core.toPath
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.text.Charsets
import kotlin.time.Duration

class ChronicleGitServiceImpl : ChronicleGitService {
    override fun getRepositoryPathFromUrl(url: String): String {
        return url.substringAfterLast('/').removeSuffix(".git")
    }

    override fun isUpdateToDate(message: String?): Boolean {
        return message.isNullOrEmpty() || message.startsWith("Already up to date")
    }

    override fun checkRemote(url: String): String {
        return lsRemote(url)
    }

    override fun syncFromRemote(url: String, parentDirectory: String): String {
        val localRepoDirectory = getRepositoryPathFromUrl(url)
        val parentPath = parentDirectory.toPath()
        val localRepoPath = parentPath.resolve(localRepoDirectory)
        return if (localRepoPath.exists()) {
            pull(url, localRepoPath)
        } else {
            clone(url, parentPath)
        }
    }

    override fun lsRemote(url: String): String {
        val commandLine = createGitCommandLine("ls-remote", url)
        return execute(commandLine, ExecutionTimeouts.normal)
    }

    override fun clone(url: String, workDirectory: Path): String {
        val commandLine = createGitCommandLine("clone", url)
            .withWorkingDirectory(workDirectory.normalize().toAbsolutePath())
        return execute(commandLine, ExecutionTimeouts.veryLong)
    }

    override fun pull(url: String, workDirectory: Path): String {
        val commandLine = createGitCommandLine("pull", url)
            .withWorkingDirectory(workDirectory.normalize().toAbsolutePath())
        return execute(commandLine, ExecutionTimeouts.veryLong)
    }

    private fun createGitCommandLine(vararg arguments: String): GeneralCommandLine {
        return GeneralCommandLine(listOf("git") + arguments.asList())
            .withEnvironment("GIT_TERMINAL_PROMPT", "0")
            .withEnvironment("GCM_INTERACTIVE", "never")
            .withEnvironment("GIT_PAGER", "cat")
            .withEnvironment("PAGER", "cat")
            .withCharset(Charsets.UTF_8)
    }

    private fun execute(commandLine: GeneralCommandLine, timeout: Duration): String {
        val output = ProgressExecutor(commandLine).withTimeout(timeout).execute()
        if (output.isCancelled) {
            ProgressManager.checkCanceled() // 如果的确被取消，则抛出 `ProcessCanceledException`
            throw IllegalStateException("Git command was cancelled: ${commandLine.commandLineString}")
        }
        if (output.isTimeout) {
            throw IllegalStateException("Git command timed out (${timeout}): ${commandLine.commandLineString}")
        }
        if (output.exitCode != 0) {
            throw IllegalStateException(
                "Git command failed (exitCode=${output.exitCode}): ${commandLine.commandLineString}\n${output.stderr.trim()}"
            )
        }
        return output.stdout.trim()
    }
}
