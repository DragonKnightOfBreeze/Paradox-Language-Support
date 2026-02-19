package icu.windea.pls.lang.tools

import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.toPath
import java.nio.file.Path
import kotlin.io.path.exists

class PlsGitServiceImpl : PlsGitService {
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
        if (!localRepoPath.exists()) {
            return clone(url, parentPath)
        } else {
            return pull(url, localRepoPath)
        }
    }

    override fun lsRemote(url: String): String {
        return executeCommand("git ls-remote $url")
    }

    override fun clone(url: String, workDirectory: Path): String {
        val wd = workDirectory.normalize().toAbsolutePath().toFile()
        return executeCommand("git clone $url", workDirectory = wd)
    }

    override fun pull(url: String, workDirectory: Path): String {
        val wd = workDirectory.normalize().toAbsolutePath().toFile()
        return executeCommand("git pull $url", workDirectory = wd)
    }
}
