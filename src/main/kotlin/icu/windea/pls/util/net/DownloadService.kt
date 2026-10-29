package icu.windea.pls.util.net

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.platform.util.progress.RawProgressReporter
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 下载服务。
 */
object DownloadService {
    private val logger = logger<DownloadService>()

    suspend fun download(
        uri: URI,
        targetPath: Path,
        settings: DownloadSettings = DownloadSettings(),
        sha256: String? = null,
        rawReporter: RawProgressReporter? = null,
    ): DownloadResult {
        val proxy = ProxyService.resolve(uri)
        val client = createHttpClient(proxy, settings)
        logger.info("Downloading $uri to $targetPath (proxy: ${ProxyService.describe(proxy)})")
        val request = HttpRequest.newBuilder(uri)
            .header("User-Agent", settings.userAgent)
            .timeout(Duration.ofMillis(settings.requestTimeout.inWholeMilliseconds))
            .GET()
            .build()
        val response = withContext(Dispatchers.IO) { client.send(request, HttpResponse.BodyHandlers.ofInputStream()) }
        if (response.statusCode() !in 200..299) {
            throw IOException("Unexpected HTTP status code ${response.statusCode()} for $uri")
        }
        val contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L)

        val digest = MessageDigest.getInstance("SHA-256")
        var downloaded = 0L
        val startedAt = System.nanoTime()
        val lastActivity = AtomicLong(startedAt)
        val body = response.body()
        val watchdog = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            val now = System.nanoTime()
            val elapsed = (now - startedAt) / 1_000_000
            val idle = (now - lastActivity.get()) / 1_000_000
            if (elapsed >= settings.maxDuration.inWholeMilliseconds || idle >= settings.stallTimeout.inWholeMilliseconds) {
                runCatching { body.close() }
            }
        }, 5, 5, TimeUnit.SECONDS)

        try {
            withContext(Dispatchers.IO) {
                body.buffered().use { input ->
                    targetPath.outputStream().buffered().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            ProgressManager.checkCanceled()
                            val length = input.read(buffer)
                            if (length < 0) break
                            lastActivity.set(System.nanoTime())
                            output.write(buffer, 0, length)
                            digest.update(buffer, 0, length)
                            downloaded += length
                            if (contentLength > 0) {
                                rawReporter?.fraction(downloaded.toDouble() / contentLength)
                            }
                            checkDownloadSpeed(downloaded, startedAt, settings)
                        }
                    }
                }
            }
            rawReporter?.fraction(1.0)
        } catch (e: Throwable) {
            targetPath.deleteIfExists()
            throw e
        } finally {
            watchdog.cancel(false)
        }

        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000
        val speed = if (elapsedMillis > 0) downloaded * 1000 / elapsedMillis else 0
        logger.info("Downloaded $uri: size=$downloaded, elapsed=${elapsedMillis}ms, speed=${speed}B/s")

        val actualSha256 = digest.digest().joinToString("") { "%02x".format(it) }
        if (sha256 != null && !actualSha256.equals(sha256, ignoreCase = true)) {
            targetPath.deleteIfExists()
            throw IOException("SHA-256 mismatch for $uri: expected=$sha256, actual=$actualSha256")
        }
        return DownloadResult(targetPath, downloaded, actualSha256)
    }

    private fun checkDownloadSpeed(downloadedBytes: Long, startedAtNanos: Long, settings: DownloadSettings) {
        val elapsedMillis = (System.nanoTime() - startedAtNanos) / 1_000_000
        if (elapsedMillis >= settings.maxDuration.inWholeMilliseconds) {
            throw IOException("Download took too long: ${elapsedMillis}ms, downloaded=$downloadedBytes")
        }
        if (elapsedMillis < settings.minSpeedCheckDelay.inWholeMilliseconds) return
        val speed = downloadedBytes * 1000 / elapsedMillis
        if (speed < settings.minSpeedBytesPerSecond) {
            throw IOException(
                "Download is too slow: ${speed}B/s after ${elapsedMillis}ms" +
                    " (threshold=${settings.minSpeedBytesPerSecond}B/s), downloaded=$downloadedBytes"
            )
        }
    }

    private fun createHttpClient(proxy: Proxy?, settings: DownloadSettings): HttpClient {
        val builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(settings.connectTimeout.inWholeMilliseconds))
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
}

/**
 * 下载配置。
 */
data class DownloadSettings(
    val connectTimeout: kotlin.time.Duration = 15.seconds,
    val requestTimeout: kotlin.time.Duration = 30.seconds,
    val minSpeedCheckDelay: kotlin.time.Duration = 10.seconds,
    val minSpeedBytesPerSecond: Long = 32L * 1024,
    val stallTimeout: kotlin.time.Duration = 60.seconds,
    val maxDuration: kotlin.time.Duration = 10.minutes,
    val userAgent: String = "Paradox-Chronicle",
)

/**
 * 下载结果。
 */
data class DownloadResult(
    val path: Path,
    val size: Long,
    val sha256: String,
)
