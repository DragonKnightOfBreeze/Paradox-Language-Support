package icu.windea.pls.lang.tools

import com.intellij.openapi.components.serviceOrNull
import icu.windea.pls.core.console.CommandExecutionException
import java.io.IOException
import java.nio.file.Path

interface PlsGitService {
    fun getRepositoryPathFromUrl(url: String): String

    fun isUpdateToDate(message: String?): Boolean

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun checkRemote(url: String): String

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun syncFromRemote(url: String, parentDirectory: String): String

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun lsRemote(url: String): String

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun clone(url: String, workDirectory: Path): String

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun pull(url: String, workDirectory: Path): String

    companion object {
        @JvmStatic
        fun getInstance(): PlsGitService = serviceOrNull() ?: PlsGitServiceImpl()
    }
}
