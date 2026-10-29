package icu.windea.pls.util.io

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.zip.ZipFile
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * 压缩包解压服务。
 */
object ArchiveService {
    private val logger = logger<ArchiveService>()

    fun extract(archivePath: Path, targetDirectory: Path, type: ArchiveType, settings: ArchiveSettings = ArchiveSettings()) {
        try {
            when (type) {
                ArchiveType.None -> return
                ArchiveType.Zip -> extractZip(archivePath, targetDirectory)
                ArchiveType.TarGz -> extractTarGz(archivePath, targetDirectory)
                ArchiveType.SevenZip -> extractSevenZip(archivePath, targetDirectory, settings)
            }
        } finally {
            if (settings.deleteArchiveAfterExtract) archivePath.deleteIfExists()
        }
    }

    fun findExecutable(targetDirectory: Path, executableName: String): Path? {
        if (!Files.isDirectory(targetDirectory)) return null
        Files.walk(targetDirectory).use { stream ->
            return stream
                .filter { Files.isRegularFile(it) && it.fileName.toString().equals(executableName, ignoreCase = true) }
                .findFirst()
                .orElse(null)
        }
    }

    fun makeExecutable(path: Path) {
        if (SystemInfo.isWindows) return
        runCatching {
            val permissions = Files.getPosixFilePermissions(path) + setOf(
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_EXECUTE,
            )
            Files.setPosixFilePermissions(path, permissions)
        }
    }

    private fun extractZip(archivePath: Path, targetDirectory: Path) {
        ZipFile(archivePath.toFile()).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val targetPath = resolveEntryPath(targetDirectory, entry.name) ?: continue
                if (entry.isDirectory) {
                    targetPath.createDirectories()
                } else {
                    targetPath.parent?.createDirectories()
                    zip.getInputStream(entry).use { input ->
                        targetPath.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }
    }

    private fun extractTarGz(archivePath: Path, targetDirectory: Path) {
        archivePath.inputStream().buffered().use { input ->
            GzipCompressorInputStream(input).use { gzip ->
                TarArchiveInputStream(gzip).use { tar ->
                    var entry = tar.nextEntry
                    while (entry != null) {
                        val targetPath = resolveEntryPath(targetDirectory, entry.name) ?: continue
                        if (entry.isDirectory) {
                            targetPath.createDirectories()
                        } else {
                            targetPath.parent?.createDirectories()
                            targetPath.outputStream().use { output -> tar.copyTo(output) }
                        }
                        entry = tar.nextEntry
                    }
                }
            }
        }
    }

    private fun extractSevenZip(archivePath: Path, targetDirectory: Path, settings: ArchiveSettings) {
        try {
            extractSevenZipWithCommons(archivePath, targetDirectory)
        } catch (e: IOException) {
            // Commons Compress 暂不支持部分 7z 的多输入/输出流编码器（例如 BCJ2），回退到外部 7z 工具
            val sevenZip = settings.sevenZipPath?.takeIf { Files.isRegularFile(it) }
                ?: (if (settings.allowExternalSevenZip) findSevenZipExecutable() else null)
                ?: throw e
            logger.info("Falling back to external 7z executable: $sevenZip")
            extractSevenZipWithCommand(sevenZip, archivePath, targetDirectory)
        }
    }

    private fun extractSevenZipWithCommons(archivePath: Path, targetDirectory: Path) {
        SevenZFile.builder().setFile(archivePath.toFile()).get().use { sevenZ ->
            while (true) {
                val entry = sevenZ.nextEntry ?: break
                val targetPath = resolveEntryPath(targetDirectory, entry.name) ?: continue
                if (entry.isDirectory) {
                    targetPath.createDirectories()
                } else {
                    targetPath.parent?.createDirectories()
                    targetPath.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val length = sevenZ.read(buffer)
                            if (length < 0) break
                            output.write(buffer, 0, length)
                        }
                    }
                }
            }
        }
    }

    private fun extractSevenZipWithCommand(sevenZip: Path, archivePath: Path, targetDirectory: Path) {
        val commandLine = GeneralCommandLine(
            sevenZip.toString(),
            "x",
            archivePath.toString(),
            "-o${targetDirectory.toAbsolutePath()}",
            "-y",
        ).withCharset(Charsets.UTF_8)
        val output = ProcessExecutor(commandLine).withTimeout(ExecutionTimeouts.veryLong).execute()
        if (output.exitCode != 0) {
            throw IOException(
                "Failed to extract ${archivePath.fileName} with $sevenZip (exitCode=${output.exitCode}): ${output.stderr.trim()}"
            )
        }
    }

    private fun findSevenZipExecutable(): Path? {
        val names = if (SystemInfo.isWindows) listOf("7z.exe", "7za.exe", "7zr.exe") else listOf("7z", "7za", "7zr")
        val pathDirs = System.getenv("PATH").orEmpty().split(java.io.File.pathSeparatorChar)
        for (dir in pathDirs) {
            if (dir.isBlank()) continue
            for (name in names) {
                val path = Path.of(dir, name)
                if (Files.isRegularFile(path)) return path
            }
        }
        if (SystemInfo.isWindows) {
            val commonPaths = listOf(
                "C:\\Program Files\\7-Zip\\7z.exe",
                "C:\\Program Files (x86)\\7-Zip\\7z.exe",
            )
            for (path in commonPaths) {
                val p = Path.of(path)
                if (Files.isRegularFile(p)) return p
            }
        }
        return null
    }

    private fun resolveEntryPath(targetDirectory: Path, entryName: String): Path? {
        val path = targetDirectory.resolve(entryName).normalize()
        if (!path.startsWith(targetDirectory)) return null
        return path
    }
}

/**
 * 压缩包类型。
 */
enum class ArchiveType {
    None,
    Zip,
    TarGz,
    SevenZip,
    ;
}

/**
 * 压缩包解压配置。
 *
 * @property sevenZipPath 用于解压不支持的 7z 编码器的外部 7z 可执行文件路径。
 * @property allowExternalSevenZip 是否允许自动查找外部 7z 工具。
 * @property deleteArchiveAfterExtract 解压后是否删除压缩包。
 */
data class ArchiveSettings(
    val sevenZipPath: Path? = System.getProperty("chronicle.external.tools.sevenZipPath")?.let(Path::of),
    val allowExternalSevenZip: Boolean = true,
    val deleteArchiveAfterExtract: Boolean = true,
)
