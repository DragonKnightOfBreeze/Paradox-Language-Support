@file:Suppress("UnstableApiUsage")

package icu.windea.pls.core.util.net

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.platform.util.progress.RawProgressReporter
import com.intellij.util.concurrency.AppExecutorUtil
import icu.windea.pls.core.util.io.deleteQuietly
import icu.windea.pls.core.util.io.moveToAtomically
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
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
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

/**
 * 下载服务。
 *
 * 支持失败重试、临时文件落位以及 SHA-256 校验。
 */
object DownloadService {
    private val logger = logger<DownloadService>()

    private const val PROGRESS_REPORT_INTERVAL_MILLIS = 150L

    suspend fun download(
        uri: URI,
        targetPath: Path,
        options: DownloadOptions = DownloadOptions.DEFAULT,
        sha256: String? = null,
        rawReporter: RawProgressReporter? = null,
    ): DownloadResult {
        val partPath = targetPath.resolveSibling("${targetPath.fileName}.part")
        val totalAttempts = 1 + options.maxRetries.coerceAtLeast(0)
        var attempt = 0
        while (true) {
            attempt++
            try {
                return withContext(Dispatchers.IO) {
                    downloadOnce(uri, targetPath, partPath, options, sha256, rawReporter)
                }
            } catch (e: ProcessCanceledException) {
                partPath.deleteQuietly()
                throw e
            } catch (e: CancellationException) {
                partPath.deleteQuietly()
                throw e
            } catch (e: IllegalArgumentException) {
                partPath.deleteQuietly()
                throw e
            } catch (e: Throwable) {
                partPath.deleteQuietly()
                if (attempt >= totalAttempts || !isRetryable(e)) {
                    // 确保不残留脏的 targetPath
                    targetPath.deleteQuietly()
                    throw e
                }
                rawReporter?.fraction(0.0)
                rawReporter?.details("Retrying ($attempt/${options.maxRetries})...")
                delay(options.retryDelay * attempt)
            }
        }
    }

    private suspend fun downloadOnce(
        uri: URI,
        targetPath: Path,
        partPath: Path,
        options: DownloadOptions,
        sha256: String?,
        rawReporter: RawProgressReporter?,
    ): DownloadResult {
        targetPath.parent?.createDirectories()
        val proxy = ProxyService.resolve(uri)
        val client = createHttpClient(proxy, options)
        logger.info("Downloading $uri to $targetPath (proxy: ${ProxyService.describe(proxy)})")
        val request = HttpRequest.newBuilder(uri)
            .header("User-Agent", options.userAgent)
            .timeout(Duration.ofMillis(options.requestTimeout.inWholeMilliseconds))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        val statusCode = response.statusCode()
        if (statusCode !in 200..299) {
            runCatching { response.body().close() }
            throw HttpStatusException(statusCode, "Unexpected HTTP status code $statusCode for $uri")
        }
        val contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L)

        val digest = MessageDigest.getInstance("SHA-256")
        var downloaded = 0L
        val startedAt = System.nanoTime()
        val lastActivity = AtomicLong(startedAt)
        val lastReportedAt = AtomicLong(startedAt)
        val body = response.body()
        // 协程被取消时立即关闭响应体，从而中断可能阻塞中的 `input.read`
        val cancellationHandle = currentCoroutineContext().job.invokeOnCompletion { cause ->
            if (cause != null) runCatching { body.close() }
        }
        val watchdog = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            val now = System.nanoTime()
            val elapsed = (now - startedAt) / 1_000_000
            val idle = (now - lastActivity.get()) / 1_000_000
            if (elapsed >= options.maxDuration.inWholeMilliseconds || idle >= options.stallTimeout.inWholeMilliseconds) {
                runCatching { body.close() }
            }
        }, 5, 5, TimeUnit.SECONDS)

        try {
            body.buffered().use { input ->
                partPath.outputStream().buffered().use { output ->
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
                        val now = System.nanoTime()
                        if ((now - lastReportedAt.get()) / 1_000_000 >= PROGRESS_REPORT_INTERVAL_MILLIS) {
                            lastReportedAt.set(now)
                            reportProgress(rawReporter, downloaded, contentLength, startedAt)
                        }
                        checkDownloadSpeed(downloaded, startedAt, options)
                    }
                    // 结束时刷新一次进度详情
                    reportProgress(rawReporter, downloaded, contentLength, startedAt)
                }
            }
            rawReporter?.fraction(1.0)
        } catch (e: Throwable) {
            partPath.deleteQuietly()
            throw e
        } finally {
            watchdog.cancel(false)
            cancellationHandle.dispose()
        }

        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000
        val speed = if (elapsedMillis > 0) downloaded * 1000 / elapsedMillis else 0
        logger.info("Downloaded $uri: size=$downloaded, elapsed=${elapsedMillis}ms, speed=${speed}B/s")

        val actualSha256 = digest.digest().joinToString("") { "%02x".format(it) }
        if (sha256 != null && !actualSha256.equals(sha256, ignoreCase = true)) {
            partPath.deleteQuietly()
            throw IOException("SHA-256 mismatch for $uri: expected=$sha256, actual=$actualSha256")
        }
        partPath.moveToAtomically(targetPath)
        return DownloadResult(targetPath, downloaded, actualSha256)
    }

    private fun reportProgress(
        rawReporter: RawProgressReporter?,
        downloadedBytes: Long,
        contentLength: Long,
        startedAtNanos: Long,
    ) {
        if (rawReporter == null) return
        val elapsedMillis = (System.nanoTime() - startedAtNanos) / 1_000_000
        val speed = if (elapsedMillis > 0) downloadedBytes * 1000 / elapsedMillis else 0L
        val details = if (contentLength > 0) {
            rawReporter.fraction(downloadedBytes.toDouble() / contentLength)
            "${formatBytes(downloadedBytes)} / ${formatBytes(contentLength)} (${formatBytes(speed)}/s)"
        } else {
            "${formatBytes(downloadedBytes)} (${formatBytes(speed)}/s)"
        }
        rawReporter.details(details)
    }

    private fun formatBytes(bytes: Long): String {
        val megabytes = bytes / (1024.0 * 1024.0)
        return if (megabytes >= 1.0) {
            String.format(Locale.ROOT, "%.1f MB", megabytes)
        } else {
            String.format(Locale.ROOT, "%.0f KB", bytes / 1024.0)
        }
    }

    private fun isRetryable(e: Throwable): Boolean {
        if (e is HttpStatusException) {
            val code = e.statusCode
            // 不可恢复的 4xx（408/429 除外）不重试
            return code !in 400..499 || code == 408 || code == 429
        }
        return true
    }

    private fun checkDownloadSpeed(downloadedBytes: Long, startedAtNanos: Long, options: DownloadOptions) {
        val elapsedMillis = (System.nanoTime() - startedAtNanos) / 1_000_000
        if (elapsedMillis >= options.maxDuration.inWholeMilliseconds) {
            throw IOException("Download took too long: ${elapsedMillis}ms, downloaded=$downloadedBytes")
        }
        if (elapsedMillis < options.minSpeedCheckDelay.inWholeMilliseconds) return
        val speed = downloadedBytes * 1000 / elapsedMillis
        if (speed < options.minSpeedBytesPerSecond) {
            throw IOException(
                "Download is too slow: ${speed}B/s after ${elapsedMillis}ms" +
                    " (threshold=${options.minSpeedBytesPerSecond}B/s), downloaded=$downloadedBytes"
            )
        }
    }

    private fun createHttpClient(proxy: Proxy?, options: DownloadOptions): HttpClient {
        val builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(options.connectTimeout.inWholeMilliseconds))
            .followRedirects(HttpClient.Redirect.NORMAL)
        if (proxy != null) {
            builder.proxy(FixedProxySelector(proxy))
        }
        return builder.build()
    }

    private class HttpStatusException(val statusCode: Int, message: String) : IOException(message)

    private class FixedProxySelector(private val proxy: Proxy) : ProxySelector() {
        override fun select(uri: URI): List<Proxy> = listOf(proxy)

        override fun connectFailed(uri: URI, sa: SocketAddress, ioe: IOException) {
            // no-op
        }
    }
}
