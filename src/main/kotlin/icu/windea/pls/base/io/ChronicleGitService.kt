package icu.windea.pls.base.io

import com.intellij.execution.ExecutionException
import com.intellij.openapi.components.serviceOrNull
import java.io.IOException
import java.nio.file.Path

interface ChronicleGitService {
    fun getRepositoryPathFromUrl(url: String): String

    fun isUpdateToDate(message: String?): Boolean

    @Throws(ExecutionException::class, IOException::class, InterruptedException::class)
    fun checkRemote(url: String): String

    @Throws(ExecutionException::class, IOException::class, InterruptedException::class)
    fun syncFromRemote(url: String, parentDirectory: String): String

    @Throws(ExecutionException::class, IOException::class, InterruptedException::class)
    fun lsRemote(url: String): String

    @Throws(ExecutionException::class, IOException::class, InterruptedException::class)
    fun clone(url: String, workDirectory: Path): String

    @Throws(ExecutionException::class, IOException::class, InterruptedException::class)
    fun pull(url: String, workDirectory: Path): String

    companion object {
        @JvmStatic
        fun getInstance(): ChronicleGitService = serviceOrNull() ?: ChronicleGitServiceImpl()
    }
}
