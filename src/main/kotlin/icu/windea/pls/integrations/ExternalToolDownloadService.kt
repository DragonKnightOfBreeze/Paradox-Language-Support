package icu.windea.pls.integrations

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.util.progress.reportRawProgress
import icu.windea.pls.util.io.ArchiveService
import icu.windea.pls.util.io.ArchiveSettings
import icu.windea.pls.util.io.ArchiveType
import icu.windea.pls.util.net.DownloadService
import icu.windea.pls.util.net.DownloadSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * 外部工具下载服务。
 *
 * 该服务只负责组合下载与解压，不绑定任何进度上下文；调用方可以自行选择
 * `withBackgroundProgress`、`withModalProgress` 或其他进度包装。
 */
@Suppress("UnstableApiUsage")
@Service
class ExternalToolDownloadService {
    /**
     * 目标路径是否为空或不存在。
     *
     * - `null` 或不存在时返回 `true`。
     * - 空目录或空文件返回 `true`。
     */
    fun check(targetPath: Path?): Boolean {
        if (targetPath == null) return true
        if (!targetPath.exists()) return true
        if (Files.isDirectory(targetPath)) {
            return Files.newDirectoryStream(targetPath).use { !it.iterator().hasNext() }
        }
        return Files.size(targetPath) == 0L
    }

    /**
     * 下载并解压外部工具，返回可执行文件路径。
     *
     * [targetDirectory] 必须为空或不存在；下载过程中的状态通过 `reportProgressScope` 报告。
     */
    suspend fun download(
        targetDirectory: Path,
        descriptor: ExternalToolDownloadDescriptor,
        downloadSettings: DownloadSettings = DownloadSettings(),
        archiveSettings: ArchiveSettings = ArchiveSettings(),
    ): Path {
        require(check(targetDirectory)) { "Target directory must be empty or nonexistent: $targetDirectory" }
        val root = targetDirectory.toAbsolutePath().normalize()
        withContext(Dispatchers.IO) { Files.createDirectories(root) }
        val downloadPath = root.resolve(descriptor.fileName)

        return reportProgressScope(2) { reporter ->
            reporter.indeterminateStep("Downloading ${descriptor.name}") {
                reportRawProgress { rawReporter ->
                    rawReporter.text("Downloading ${descriptor.name}")
                    rawReporter.details(descriptor.url)
                    DownloadService.download(
                        uri = URI(descriptor.url),
                        targetPath = downloadPath,
                        settings = downloadSettings,
                        sha256 = descriptor.sha256,
                        rawReporter = rawReporter,
                    )
                }
            }
            reporter.indeterminateStep("Extracting ${descriptor.name}") {
                withContext(Dispatchers.IO) {
                    ArchiveService.extract(downloadPath, root, descriptor.archiveType, archiveSettings)
                    val executable = ArchiveService.findExecutable(root, descriptor.executableName)
                        ?: throw IllegalStateException("Executable ${descriptor.executableName} is missing after downloading ${descriptor.name}.")
                    ArchiveService.makeExecutable(executable)
                    executable
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): ExternalToolDownloadService = service()
    }
}

/**
 * 外部工具下载描述。
 *
 * @property name 工具名称。
 * @property url 下载地址。
 * @property fileName 下载文件的名称。
 * @property executableName 可执行文件的名称。
 * @property archiveType 压缩包类型；为 [ArchiveType.None] 时表示直接下载文件。
 * @property sha256 可选的 SHA-256 校验值。
 * @property version 可选的版本标识，用于测试缓存判断。
 */
data class ExternalToolDownloadDescriptor(
    val name: String,
    val url: String,
    val fileName: String,
    val executableName: String,
    val archiveType: ArchiveType = ArchiveType.None,
    val sha256: String? = null,
    val version: String? = null,
)
