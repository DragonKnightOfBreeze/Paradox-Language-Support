package icu.windea.pls.util.net

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.seconds

/**
 * @see DownloadService
 */
@RunWith(JUnit4::class)
class DownloadServiceTest : BasePlatformTestCase() {
    @Test
    fun download_smoke_test() {
        val content = "hello world"
        val sha256 = MessageDigest.getInstance("SHA-256").digest(content.toByteArray()).joinToString("") { "%02x".format(it) }
        withServer(content) { uri ->
            val target = targetPath("download.txt")
            val result = runBlocking {
                DownloadService.download(
                    uri = uri,
                    targetPath = target,
                    settings = DownloadSettings(minSpeedCheckDelay = 1.seconds, stallTimeout = 30.seconds),
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
            val target = targetPath("mismatch.txt")
            val result = runCatching {
                runBlocking {
                    DownloadService.download(
                        uri = uri,
                        targetPath = target,
                        settings = DownloadSettings(minSpeedCheckDelay = 1.seconds, stallTimeout = 30.seconds),
                        sha256 = "0000000000000000000000000000000000000000000000000000000000000000",
                    )
                }
            }
            Assert.assertTrue(result.isFailure)
            Assert.assertFalse(Files.exists(target))
        }
    }

    private fun withServer(content: String, action: (java.net.URI) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/file") { exchange ->
            val bytes = content.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/octet-stream")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            val port = server.address.port
            action(java.net.URI("http://127.0.0.1:$port/file"))
        } finally {
            server.stop(0)
        }
    }

    private fun targetPath(fileName: String): Path {
        val dir = Path.of("build", "tmp", "download-service-test").also { it.createDirectories() }
        return dir.resolve(fileName).also { it.deleteIfExists() }
    }
}
