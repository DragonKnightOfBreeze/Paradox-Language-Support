package icu.windea.pls.core.util.io

import com.intellij.openapi.progress.ProcessCanceledException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * @see IoService
 */
@RunWith(JUnit4::class)
class IoServiceTest {
    @Test
    fun inputStream_checksCancellationAfterThreshold_test() {
        var checkedCount = 0
        val input = CancellableInputStream(ByteArrayInputStream(ByteArray(10)), threshold = 4) { checkedCount++ }
        val buffer = ByteArray(3)

        input.read(buffer)
        Assert.assertEquals(0, checkedCount) // 3 字节，未达到阈值

        input.read(buffer)
        Assert.assertEquals(1, checkedCount) // 累计 6 字节，达到阈值

        input.read(buffer)
        input.read(buffer)
        Assert.assertEquals(2, checkedCount) // 再次累计达到阈值
    }

    @Test
    fun inputStream_cancelAtThreshold_test() {
        val input = CancellableInputStream(ByteArrayInputStream(ByteArray(10)), threshold = 4) {
            throw ProcessCanceledException()
        }

        Assert.assertThrows(ProcessCanceledException::class.java) {
            input.read(ByteArray(4))
        }
    }

    @Test
    fun outputStream_checksCancellationAfterThreshold_test() {
        var checkedCount = 0
        val output = CancellableOutputStream(ByteArrayOutputStream(), threshold = 4) { checkedCount++ }

        output.write(ByteArray(3))
        Assert.assertEquals(0, checkedCount) // 3 字节，未达到阈值

        output.write(ByteArray(3))
        Assert.assertEquals(1, checkedCount) // 累计 6 字节，达到阈值

        output.write(ByteArray(3))
        output.write(ByteArray(3))
        Assert.assertEquals(2, checkedCount) // 再次累计达到阈值
    }

    @Test
    fun outputStream_cancelAtThreshold_test() {
        val output = CancellableOutputStream(ByteArrayOutputStream(), threshold = 4) {
            throw ProcessCanceledException()
        }

        Assert.assertThrows(ProcessCanceledException::class.java) {
            output.write(ByteArray(4))
        }
    }

    @Test
    fun copyPath_test() {
        val dir = Path.of("build", "tmp", "io-service-test")
        dir.toFile().deleteRecursively()
        val source = dir.resolve("source.txt")
        val target = dir.resolve("nested/target.txt")
        Files.createDirectories(source.parent)
        Files.writeString(source, "hello")

        val bytesCopied = IoService.copy(source, target)

        Assert.assertEquals(5L, bytesCopied)
        Assert.assertEquals("hello", Files.readString(target))
    }

    @Test
    fun copyPath_samePath_test() {
        val source = Path.of("build", "tmp", "io-service-test", "same.txt")
        Files.createDirectories(source.parent)
        Files.writeString(source, "hello")

        val bytesCopied = IoService.copy(source, source)

        Assert.assertEquals(-1L, bytesCopied)
        Assert.assertEquals("hello", Files.readString(source))
    }

    @Test
    fun copyStream_withProgressChecking_test() {
        // 默认阈值的复制路径仍应正确复制全部字节
        val size = IO_CANCELLATION_THRESHOLD.toInt() * 2
        val bytes = ByteArray(size) { it.toByte() }
        val input = ByteArrayInputStream(bytes)
        val output = ByteArrayOutputStream()

        IoService.copy(input, output)

        Assert.assertEquals(size, output.size())
        Assert.assertArrayEquals(bytes, output.toByteArray())
    }
}
