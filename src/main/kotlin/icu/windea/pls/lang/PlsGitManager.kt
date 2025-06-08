package icu.windea.pls.lang

import icu.windea.pls.core.*
import icu.windea.pls.core.console.CommandExecutionException
import icu.windea.pls.core.console.CommandType
import java.io.*
import java.nio.file.*
import kotlin.io.path.exists

object PlsGitManager {
    fun getRepositoryPathFromUrl(url: String): String {
        return url.substringAfterLast('/').removeSuffix(".git")
    }

    fun isUpdateToDate(message: String?): Boolean {
        return message.isNullOrEmpty() || message.startsWith("Already up to date")
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun checkRemote(url: String): String {
        return lsRemote(url)
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun syncFromRemote(url: String, parentDirectory: String): String {
        val localRepoDirectory = getRepositoryPathFromUrl(url)
        val parentPath = parentDirectory.toPath()
        val localRepoPath = parentPath.resolve(localRepoDirectory)
        if (!localRepoPath.exists()) {
            return clone(url, parentPath)
        } else {
            return pull(url, localRepoPath)
        }
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun lsRemote(url: String): String {
        return executeCommand("git ls-remote $url")
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun clone(url: String, workDirectory: Path): String {
        val wd = workDirectory.normalize().toAbsolutePath().toFile()
        return executeCommand("git clone $url", workDirectory = wd)
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun pull(url: String, workDirectory: Path): String {
        val wd = workDirectory.normalize().toAbsolutePath().toFile()
        return executeCommand("git pull $url", workDirectory = wd)
    }
}
