package icu.windea.pls.integrations

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.core.util.io.ArchiveType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.io.path.writeText

/**
 * 外部工具下载测试服务。
 *
 * 所有测试共享同一个 `build/external-tools` 目录；同一种工具只会下载一次。
 * 下载目录中会保留一个 `.downloaded-tool` 标记，用于在描述不一致时自动重新下载。
 *
 * @see ExternalToolDownloadService
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

    val ck3Tiger: ExternalToolDownloadDescriptor? = tigerDescriptor(
        name = "ck3-tiger",
        executableName = "ck3-tiger",
        windowsSha256 = "ddafd73abcff8e802b82bf3ae622d02c04dfc8a9dd0cad44b2dbf67568a10505",
        linuxSha256 = "6d72d276c2b2a953f63221ff8050d1276a085b5a2ffe74977675fdf6e17e9ff3",
    )

    val vic3Tiger: ExternalToolDownloadDescriptor? = tigerDescriptor(
        name = "vic3-tiger",
        executableName = "vic3-tiger",
        windowsSha256 = "45022a8fd91ed817f49712f35626d05c142715c8d0dc715c7b5bb7ac926ef8d7",
        linuxSha256 = "eff63a3f74646634f81d0f2fc4e7f631c2b9276055ce3cd0e0cace0866c64ec4",
    )

    val irTiger: ExternalToolDownloadDescriptor? = tigerDescriptor(
        name = "imperator-tiger",
        executableName = "imperator-tiger",
        windowsSha256 = "f5d3537ab05fc590b09997700bddcac80bbcf41986713d2bf9e2a2643e5b9444",
        linuxSha256 = "591a88df9a58f9788cae5b9e47715fa7835949622a6b2a876f85a7b5ac06ddeb",
    )

    private fun tigerDescriptor(
        name: String,
        executableName: String,
        windowsSha256: String,
        linuxSha256: String,
        version: String = "v1.19.0",
    ): ExternalToolDownloadDescriptor? {
        return when {
            SystemInfo.isWindows -> ExternalToolDownloadDescriptor(
                name = name,
                url = "https://github.com/amtep/tiger/releases/download/$version/$name-windows-$version.zip",
                fileName = "$name-windows-$version.zip",
                executableName = "$executableName.exe",
                archiveType = ArchiveType.Zip,
                sha256 = windowsSha256,
                version = version,
            )
            SystemInfo.isLinux -> ExternalToolDownloadDescriptor(
                name = name,
                url = "https://github.com/amtep/tiger/releases/download/$version/$name-linux-$version.tar.gz",
                fileName = "$name-linux-$version.tar.gz",
                executableName = executableName,
                archiveType = ArchiveType.TarGz,
                sha256 = linuxSha256,
                version = version,
            )
            else -> null
        }
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
        if (!targetDirectory.isDirectory()) return null
        return targetDirectory.walk().find { it.isRegularFile() && it.name.equals(executableName, ignoreCase = true) }
    }
}
