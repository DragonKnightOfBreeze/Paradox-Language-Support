package icu.windea.pls.integrations

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.platform.util.progress.RawProgressReporter
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.util.progress.reportRawProgress
import com.intellij.util.net.ProxyConfiguration
import com.intellij.util.net.ProxySettings
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.security.MessageDigest
import java.time.Duration
import java.util.zip.ZipFile
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * 外部工具下载服务。
 *
 * 该服务只负责下载和解压，不绑定任何进度上下文；调用方可以自行选择
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
    suspend fun download(targetDirectory: Path, descriptor: ExternalToolDownloadDescriptor): Path {
        require(check(targetDirectory)) { "Target directory must be empty or nonexistent: $targetDirectory" }
        val root = targetDirectory.toAbsolutePath().normalize()
        withContext(Dispatchers.IO) { Files.createDirectories(root) }
        val downloadPath = root.resolve(descriptor.fileName)

        return reportProgressScope(2) { reporter ->
            reporter.indeterminateStep("Downloading ${descriptor.name}") {
                reportRawProgress { rawReporter ->
                    rawReporter.text("Downloading ${descriptor.name}")
                    rawReporter.details(descriptor.url)
                    withContext(Dispatchers.IO) { downloadFile(descriptor, downloadPath, rawReporter) }
                }
            }
            reporter.indeterminateStep("Extracting ${descriptor.name}") {
                withContext(Dispatchers.IO) {
                    extract(descriptor, downloadPath, root)
                    val executable = findExecutable(root, descriptor.executableName)
                        ?: throw IllegalStateException("Executable ${descriptor.executableName} is missing after downloading ${descriptor.name}.")
                    makeExecutable(executable)
                    executable
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadFile(descriptor: ExternalToolDownloadDescriptor, targetPath: Path, rawReporter: RawProgressReporter) {
        val uri = URI(descriptor.url)
        val proxy = resolveProxy(uri)
        val client = createHttpClient(proxy)

        logger.info(
            "Downloading ${descriptor.name} from ${descriptor.url} to $targetPath" +
                " (proxy: ${proxyDescription(proxy)})"
        )
        val request = HttpRequest.newBuilder(uri)
            .header("User-Agent", "Paradox-Chronicle")
            .timeout(Duration.ofMillis(READ_TIMEOUT_MILLIS.toLong()))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() !in 200..299) {
            throw IOException("Unexpected HTTP status code ${response.statusCode()} for ${descriptor.url}")
        }
        val contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L)

        val digest = MessageDigest.getInstance("SHA-256")
        var downloaded = 0L
        val startedAt = System.nanoTime()
        try {
            response.body().buffered().use { input ->
                targetPath.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        ProgressManager.checkCanceled()
                        val length = input.read(buffer)
                        if (length < 0) break
                        output.write(buffer, 0, length)
                        digest.update(buffer, 0, length)
                        downloaded += length
                        if (contentLength > 0) {
                            rawReporter.fraction(downloaded.toDouble() / contentLength)
                        }
                        checkDownloadSpeed(downloaded, startedAt)
                    }
                }
            }
            rawReporter.fraction(1.0)
        } catch (e: Throwable) {
            targetPath.deleteIfExists()
            throw e
        }

        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000
        val speed = if (elapsedMillis > 0) downloaded * 1000 / elapsedMillis else 0
        logger.info("Downloaded ${descriptor.name}: size=$downloaded, elapsed=${elapsedMillis}ms, speed=${speed}B/s")

        val expected = descriptor.sha256
        if (expected != null) {
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actual.equals(expected, ignoreCase = true)) {
                targetPath.deleteIfExists()
                throw IOException("SHA-256 mismatch for ${descriptor.name}: expected=$expected, actual=$actual")
            }
        }
    }

    private fun checkDownloadSpeed(downloadedBytes: Long, startedAtNanos: Long) {
        val elapsedMillis = (System.nanoTime() - startedAtNanos) / 1_000_000
        if (elapsedMillis >= MAX_DOWNLOAD_DURATION_MILLIS) {
            throw IOException("Download took too long: ${elapsedMillis}ms, downloaded=$downloadedBytes")
        }
        if (elapsedMillis < MIN_SPEED_CHECK_DELAY_MILLIS) return
        val speed = downloadedBytes * 1000 / elapsedMillis
        if (speed < MIN_DOWNLOAD_SPEED_BYTES_PER_SECOND) {
            throw IOException(
                "Download is too slow: ${speed}B/s after ${elapsedMillis}ms" +
                    " (threshold=${MIN_DOWNLOAD_SPEED_BYTES_PER_SECOND}B/s), downloaded=$downloadedBytes"
            )
        }
    }

    private fun extract(descriptor: ExternalToolDownloadDescriptor, archivePath: Path, targetDirectory: Path) {
        when (descriptor.archiveType) {
            ExternalToolArchiveType.None -> return
            ExternalToolArchiveType.Zip -> extractZip(archivePath, targetDirectory)
            ExternalToolArchiveType.TarGz -> extractTarGz(archivePath, targetDirectory)
            ExternalToolArchiveType.SevenZip -> extractSevenZip(archivePath, targetDirectory)
        }
        archivePath.deleteIfExists()
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

    private fun extractSevenZip(archivePath: Path, targetDirectory: Path) {
        try {
            extractSevenZipWithCommons(archivePath, targetDirectory)
        } catch (e: IOException) {
            // Commons Compress 暂不支持部分 7z 的多输入/输出流编码器（例如 BCJ2），回退到外部 7z 工具
            val sevenZip = findSevenZipExecutable() ?: throw e
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
        System.getProperty("chronicle.external.tools.sevenZipPath")?.let {
            val path = Path.of(it)
            if (Files.isRegularFile(path)) return path
        }
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

    private fun findExecutable(targetDirectory: Path, executableName: String): Path? {
        if (!Files.isDirectory(targetDirectory)) return null
        Files.walk(targetDirectory).use { stream ->
            return stream
                .filter { Files.isRegularFile(it) && it.fileName.toString().equals(executableName, ignoreCase = true) }
                .findFirst()
                .orElse(null)
        }
    }

    private fun makeExecutable(path: Path) {
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

    private fun createHttpClient(proxy: Proxy?): HttpClient {
        val builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MILLIS.toLong()))
            .followRedirects(HttpClient.Redirect.NORMAL)
        if (proxy != null) {
            builder.proxy(FixedProxySelector(proxy))
        }
        return builder.build()
    }

    private class FixedProxySelector(private val proxy: Proxy) : ProxySelector() {
        override fun select(uri: URI): List<Proxy> = listOf(proxy)

        override fun connectFailed(uri: URI, sa: SocketAddress, ioe: IOException) {
            // no-op
        }
    }

    private fun resolveProxy(uri: URI): Proxy? {
        // 优先使用 IDE 中显式配置的静态代理
        val platformProxy = runCatching { ProxySettings.getInstance().getProxyConfiguration() }.getOrNull()
        if (platformProxy is ProxyConfiguration.StaticProxyConfiguration) {
            return createProxy(platformProxy.host, platformProxy.port, platformProxy.protocol)
        }

        // 自动应用系统代理（环境变量、系统属性、平台代理选择器）
        resolveProxyFromEnvironment(uri)?.let { return it }
        runCatching {
            ProxySelector.getDefault()?.select(uri)?.firstOrNull { it.type() != Proxy.Type.DIRECT }
        }.getOrNull()?.let { return it }
        resolveProxyFromSystemProperties(uri)?.let { return it }
        return null
    }

    private fun resolveProxyFromEnvironment(uri: URI): Proxy? {
        val scheme = uri.scheme.lowercase()
        val value = when (scheme) {
            "https" -> getEnvProxy("HTTPS_PROXY") ?: getEnvProxy("HTTP_PROXY") ?: getEnvProxy("ALL_PROXY")
            else -> getEnvProxy("HTTP_PROXY") ?: getEnvProxy("ALL_PROXY")
        } ?: return null
        if (isProxyExcluded(uri.host)) return null
        return parseProxyUrl(value)
    }

    private fun getEnvProxy(name: String): String? {
        return System.getenv(name) ?: System.getenv(name.lowercase())
    }

    private fun resolveProxyFromSystemProperties(uri: URI): Proxy? {
        val scheme = uri.scheme.lowercase()
        val host = System.getProperty("$scheme.proxyHost") ?: System.getProperty("http.proxyHost") ?: return null
        val port = (System.getProperty("$scheme.proxyPort") ?: System.getProperty("http.proxyPort"))?.toIntOrNull() ?: 80
        return createProxy(host, port, ProxyConfiguration.ProxyProtocol.HTTP)
    }

    private fun parseProxyUrl(value: String): Proxy? {
        val normalized = if (value.contains("://")) value else "http://$value"
        return runCatching {
            val uri = URI(normalized)
            val port = if (uri.port > 0) uri.port else 80
            createProxy(uri.host ?: return null, port, ProxyConfiguration.ProxyProtocol.HTTP)
        }.getOrNull()
    }

    private fun createProxy(host: String, port: Int, protocol: ProxyConfiguration.ProxyProtocol): Proxy {
        val type = if (protocol == ProxyConfiguration.ProxyProtocol.SOCKS) Proxy.Type.SOCKS else Proxy.Type.HTTP
        return Proxy(type, InetSocketAddress(host, port))
    }

    private fun isProxyExcluded(host: String?): Boolean {
        if (host.isNullOrEmpty()) return false
        val noProxy = getEnvProxy("NO_PROXY") ?: return false
        return noProxy.split(',').any { pattern ->
            val p = pattern.trim()
            if (p.isEmpty()) return@any false
            when {
                p == "*" -> true
                p.startsWith(".") -> host.endsWith(p, ignoreCase = true)
                else -> host.equals(p, ignoreCase = true) || host.endsWith(".$p", ignoreCase = true)
            }
        }
    }

    private fun proxyDescription(proxy: Proxy?): String {
        if (proxy == null) return "direct"
        val address = proxy.address() as? InetSocketAddress ?: return proxy.toString()
        return "${proxy.type().name.lowercase()}://${address.hostString}:${address.port}"
    }

    companion object {
        private val logger = logger<ExternalToolDownloadService>()

        private const val CONNECT_TIMEOUT_MILLIS = 15_000
        private const val READ_TIMEOUT_MILLIS = 30_000
        private const val MIN_SPEED_CHECK_DELAY_MILLIS = 10_000L
        private const val MIN_DOWNLOAD_SPEED_BYTES_PER_SECOND = 32L * 1024
        private const val MAX_DOWNLOAD_DURATION_MILLIS = 10L * 60 * 1000

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
 * @property archiveType 压缩包类型；为 [ExternalToolArchiveType.None] 时表示直接下载文件。
 * @property sha256 可选的 SHA-256 校验值。
 * @property version 可选的版本标识，用于测试缓存判断。
 */
data class ExternalToolDownloadDescriptor(
    val name: String,
    val url: String,
    val fileName: String,
    val executableName: String,
    val archiveType: ExternalToolArchiveType = ExternalToolArchiveType.None,
    val sha256: String? = null,
    val version: String? = null,
)

enum class ExternalToolArchiveType {
    None,
    Zip,
    TarGz,
    SevenZip,
    ;
}
