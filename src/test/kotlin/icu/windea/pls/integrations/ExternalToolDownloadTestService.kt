package icu.windea.pls.integrations

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.util.io.ArchiveType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * 外部工具下载测试服务。
 *
 * 所有测试共享同一个 `build/external-tools` 目录；同一种工具只会下载一次。
 * 下载目录中会保留一个 `.downloaded-tool` 标记，用于在描述不一致时自动重新下载。
 */
object ExternalToolDownloadTestService {
    val root: Path = Path.of("build", "external-tools").toAbsolutePath().normalize()

    val texconv: ExternalToolDownloadDescriptor? = if (SystemInfo.isWindows) {
        ExternalToolDownloadDescriptor(
            name = "texconv",
            url = "https://github.com/microsoft/DirectXTex/releases/download/may2026/texconv.exe",
            fileName = "texconv.exe",
            executableName = "texconv.exe",
            sha256 = "dcfdec10244e02cf5037fba089c55fb7e1326b1c8181742d77d15fa5cb5eef06",
            version = "may2026",
        )
    } else {
        null // Texconv is Windows-only.
    }

    val magick: ExternalToolDownloadDescriptor? = when {
        SystemInfo.isWindows -> ExternalToolDownloadDescriptor(
            name = "magick",
            url = "https://github.com/ImageMagick/ImageMagick/releases/download/7.1.2-34/ImageMagick-7.1.2-34-portable-Q16-x64.7z",
            fileName = "ImageMagick-7.1.2-34-portable-Q16-x64.7z",
            executableName = "magick.exe",
            archiveType = ArchiveType.SevenZip,
            sha256 = "52cc6a441c7884f5ff4274b410c9b90c4b7d54245dcdf4d32988bb4e4df4e5d1",
            version = "7.1.2-34",
        )
        SystemInfo.isLinux -> ExternalToolDownloadDescriptor(
            name = "magick",
            url = "https://github.com/ImageMagick/ImageMagick/releases/download/7.1.2-34/ImageMagick-7.1.2-34-gcc-x86_64.AppImage",
            fileName = "ImageMagick-7.1.2-34-gcc-x86_64.AppImage",
            executableName = "ImageMagick-7.1.2-34-gcc-x86_64.AppImage",
            sha256 = "6e9edfd35d5f30b57c27533afe003d6085faaf8607b207426f398d11751acf93",
            version = "7.1.2-34",
        )
        else -> null
    }

    private val sevenZipR: ExternalToolDownloadDescriptor? = if (SystemInfo.isWindows) {
        ExternalToolDownloadDescriptor(
            name = "7zr",
            url = "https://www.7-zip.org/a/7zr.exe",
            fileName = "7zr.exe",
            executableName = "7zr.exe",
        )
    } else {
        null
    }

    val ck3Tiger: ExternalToolDownloadDescriptor? = when {
        SystemInfo.isWindows -> ExternalToolDownloadDescriptor(
            name = "ck3-tiger",
            url = "https://github.com/amtep/tiger/releases/download/v1.19.0/ck3-tiger-windows-v1.19.0.zip",
            fileName = "ck3-tiger-windows-v1.19.0.zip",
            executableName = "ck3-tiger.exe",
            archiveType = ArchiveType.Zip,
            sha256 = "ddafd73abcff8e802b82bf3ae622d02c04dfc8a9dd0cad44b2dbf67568a10505",
            version = "v1.19.0",
        )
        SystemInfo.isLinux -> ExternalToolDownloadDescriptor(
            name = "ck3-tiger",
            url = "https://github.com/amtep/tiger/releases/download/v1.19.0/ck3-tiger-linux-v1.19.0.tar.gz",
            fileName = "ck3-tiger-linux-v1.19.0.tar.gz",
            executableName = "ck3-tiger",
            archiveType = ArchiveType.TarGz,
            sha256 = "6d72d276c2b2a953f63221ff8050d1276a085b5a2ffe74977675fdf6e17e9ff3",
            version = "v1.19.0",
        )
        else -> null
    }

    private val locks = ConcurrentHashMap<String, Mutex>()
    private const val MARKER_FILE_NAME = ".downloaded-tool"

    fun ensureTool(descriptor: ExternalToolDownloadDescriptor): Path {
        return runBlocking { ensureToolSuspend(descriptor) }
    }

    suspend fun ensureToolSuspend(descriptor: ExternalToolDownloadDescriptor): Path {
        val targetDirectory = root.resolve(descriptor.name)
        findExecutable(targetDirectory, descriptor.executableName)?.let {
            if (isUpToDate(targetDirectory, descriptor)) {
                println("[external-tools] Reuse ${descriptor.name}: $it")
                return it
            }
            println("[external-tools] Outdated ${descriptor.name} detected, removing: $targetDirectory")
        }
        return locks.getOrPut(descriptor.name) { Mutex() }.withLock {
            findExecutable(targetDirectory, descriptor.executableName)?.let {
                if (isUpToDate(targetDirectory, descriptor)) {
                    println("[external-tools] Reuse ${descriptor.name}: $it")
                    return@withLock it
                }
                println("[external-tools] Outdated ${descriptor.name} detected, removing: $targetDirectory")
            }
            if (!ExternalToolDownloadService.getInstance().check(targetDirectory)) {
                targetDirectory.toFile().deleteRecursively()
            }
            if (descriptor.archiveType == ArchiveType.SevenZip && SystemInfo.isWindows) {
                val sevenZip = sevenZipR ?: error("7zr descriptor is missing")
                val sevenZipPath = ensureToolSuspend(sevenZip)
                System.setProperty("chronicle.external.tools.sevenZipPath", sevenZipPath.toString())
                println("[external-tools] Using 7zr: $sevenZipPath")
            }
            println("[external-tools] Downloading ${descriptor.name}")
            println("[external-tools] URL: ${descriptor.url}")
            println("[external-tools] Target directory: $targetDirectory")
            val executable = ExternalToolDownloadService.getInstance().download(targetDirectory, descriptor)
            writeMarker(targetDirectory, descriptor)
            println("[external-tools] Downloaded ${descriptor.name}: $executable (${Files.size(executable)} bytes)")
            executable
        }
    }

    fun runSmokeTest(executablePath: Path, vararg arguments: String) {
        val commandLine = GeneralCommandLine(listOf(executablePath.toString()) + arguments)
        if (executablePath.fileName.toString().endsWith(".AppImage", ignoreCase = true)) {
            commandLine.withEnvironment("APPIMAGE_EXTRACT_AND_RUN", "1")
        }
        println("[external-tools] Smoke test: ${commandLine.commandLineString}")
        val output = ProcessExecutor(commandLine).withTimeout(ExecutionTimeouts.normal).execute()
        println("[external-tools] Smoke test result: exitCode=${output.exitCode}")
        Assert.assertEquals("Unexpected exit code for $executablePath ${arguments.joinToString(" ")}", 0, output.exitCode)
    }

    private fun isUpToDate(targetDirectory: Path, descriptor: ExternalToolDownloadDescriptor): Boolean {
        val marker = targetDirectory.resolve(MARKER_FILE_NAME)
        if (!marker.exists()) return false
        val expected = markerContent(descriptor)
        return runCatching { marker.readText() }.getOrNull() == expected
    }

    private fun writeMarker(targetDirectory: Path, descriptor: ExternalToolDownloadDescriptor) {
        targetDirectory.resolve(MARKER_FILE_NAME).writeText(markerContent(descriptor))
    }

    private fun markerContent(descriptor: ExternalToolDownloadDescriptor): String {
        return buildString {
            appendLine("name=${descriptor.name}")
            appendLine("url=${descriptor.url}")
            appendLine("sha256=${descriptor.sha256.orEmpty()}")
            appendLine("version=${descriptor.version.orEmpty()}")
        }
    }

    private fun findExecutable(targetDirectory: Path, executableName: String): Path? {
        if (!targetDirectory.exists()) return null
        Files.walk(targetDirectory).use { stream ->
            return stream
                .filter { Files.isRegularFile(it) && it.fileName.toString().equals(executableName, ignoreCase = true) }
                .findFirst()
                .orElse(null)
        }
    }
}
