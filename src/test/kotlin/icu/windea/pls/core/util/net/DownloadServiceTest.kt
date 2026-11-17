package icu.windea.pls.core.util.net

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * @see DownloadService
 */
@RunWith(JUnit4::class)
class DownloadServiceTest  {
    private val root = Path.of("build/tmp/download-service-test")

    @Before
    fun setUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @Test
    fun download_smoke_test() {
        val content = "hello world"
        val sha256 = MessageDigest.getInstance("SHA-256").digest(content.toByteArray()).joinToString("") { "%02x".format(it) }
        withServer(content) { uri ->
            val target = root.resolve("download.txt")
            val result = runBlocking {
                DownloadService.download(
                    uri = uri,
                    targetPath = target,
                    options = DownloadOptions(minSpeedCheckDelay = 1.seconds, stallTimeout = 30.seconds),
                    sha256 = sha256,
                )
            }
            Assert.assertEquals(target, result.path)
            Assert.assertEquals(content.length.toLong(), result.size)
            Assert.assertEquals(sha256, result.sha256)
            Assert.assertEquals(content, target.readText())
        }
    }

    @Test
    fun download_sha256Mismatch_test() {
        withServer("hello") { uri ->
            val target = root.resolve("mismatch.txt")
            val result = runCatching {
                runBlocking {
                    DownloadService.download(
                        uri = uri,
                        targetPath = target,
                        // 不重试，避免无效等待
                        options = DownloadOptions(
                            minSpeedCheckDelay = 1.seconds,
                            stallTimeout = 30.seconds,
                            maxRetries = 0,
                        ),
                        sha256 = "0000000000000000000000000000000000000000000000000000000000000000",
                    )
                }
            }
            Assert.assertTrue(result.isFailure)
            Assert.assertFalse(Files.exists(target))
            Assert.assertFalse(Files.exists(root.resolve("mismatch.txt.part")))
        }
    }

    @Test
    fun download_retriesTransientFailure_test() {
        val content = "hello retry"
        val attempts = AtomicInteger()
        withServer({ exchange ->
            val attempt = attempts.incrementAndGet()
            if (attempt < 3) {
                // 前两次返回 5xx，触发重试
                exchange.sendResponseHeaders(500, -1)
                exchange.close()
            } else {
                val bytes = content.toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/octet-stream")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
        }) { uri ->
            val target = root.resolve("retry.txt")
            val result = runBlocking {
                DownloadService.download(
                    uri = uri,
                    targetPath = target,
                    options = DownloadOptions(
                        minSpeedCheckDelay = 1.seconds,
                        stallTimeout = 30.seconds,
                        maxRetries = 2,
                        retryDelay = 10.milliseconds,
                    ),
                )
            }
            Assert.assertEquals(3, attempts.get())
            Assert.assertEquals(target, result.path)
            Assert.assertEquals(content.length.toLong(), result.size)
            Assert.assertEquals(content, target.readText())
            Assert.assertFalse(Files.exists(root.resolve("retry.txt.part")))
        }
    }

    @Test
    fun download_doesNotRetryClientError_test() {
        val attempts = AtomicInteger()
        withServer({ exchange ->
            attempts.incrementAndGet()
            // 404 属于不可恢复的 4xx，不应重试
            exchange.sendResponseHeaders(404, -1)
            exchange.close()
        }) { uri ->
            val target = root.resolve("missing.txt")
            val result = runCatching {
                runBlocking {
                    DownloadService.download(
                        uri = uri,
                        targetPath = target,
                        options = DownloadOptions(
                            minSpeedCheckDelay = 1.seconds,
                            stallTimeout = 30.seconds,
                            maxRetries = 3,
                            retryDelay = 10.milliseconds,
                        ),
                    )
                }
            }
            Assert.assertTrue(result.isFailure)
            Assert.assertEquals(1, attempts.get())
            Assert.assertFalse(Files.exists(target))
            Assert.assertFalse(Files.exists(root.resolve("missing.txt.part")))
        }
    }

    private fun withServer(content: String, action: (URI) -> Unit) {
        withServer({ exchange ->
            val bytes = content.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/octet-stream")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }, action)
    }

    private fun withServer(handler: (HttpExchange) -> Unit, action: (URI) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/file") { exchange -> handler(exchange) }
        server.start()
        try {
            val port = server.address.port
            val uri = URI("http://127.0.0.1:$port/file")
            // 本地测试不假定系统代理一定存在，也不假定其不存在；如果存在代理则跳过
            Assume.assumeTrue("System proxy is configured; skip local download test", ProxyService.resolve(uri) == null)
            action(uri)
        } finally {
            server.stop(0)
        }
    }
}
