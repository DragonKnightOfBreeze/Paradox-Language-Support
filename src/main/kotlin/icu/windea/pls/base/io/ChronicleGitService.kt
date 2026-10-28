package icu.windea.pls.base.io

import com.intellij.execution.ExecutionException
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Git 服务。
 *
 * 执行命令行的方法在进程无法启动时会抛出 [ExecutionException]；
 * 命令超时、取消或返回非零退出码时，当前实现会抛出 [IllegalStateException]。
 */
interface ChronicleGitService {
    fun getRepositoryPathFromUrl(url: String): String

    fun isUpdateToDate(message: String?): Boolean

    /**
     * 得到仓库的远程地址列表。
     *
     * 备注：直接读取 `.git/config`，不会执行命令行。
     */
    fun getRemoteUrls(repositoryRoot: VirtualFile): List<String>

    @Throws(ExecutionException::class)
    fun checkRemote(url: String): String

    @Throws(ExecutionException::class)
    fun syncFromRemote(url: String, parentDirectory: String): String

    @Throws(ExecutionException::class)
    fun lsRemote(url: String): String

    @Throws(ExecutionException::class)
    fun clone(url: String, workDirectory: Path): String

    @Throws(ExecutionException::class)
    fun pull(url: String, workDirectory: Path): String

    companion object {
        @JvmStatic
        fun getInstance(): ChronicleGitService = serviceOrNull() ?: ChronicleGitServiceImpl()
    }
}
