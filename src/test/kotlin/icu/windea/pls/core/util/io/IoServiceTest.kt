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
    fun inputStream_monitorAtThreshold_test() {
        val events = mutableListOf<Pair<Long, Long>>()
        val input = MonitorableInputStream(ByteArrayInputStream(ByteArray(10)), totalBytes = 10, threshold = 4) { read, total ->
            events += read to total
        }
        val buffer = ByteArray(3)

        input.read(buffer) // 3 字节，未达到阈值
        Assert.assertTrue(events.isEmpty())

        input.read(buffer) // 累计 6 字节，达到阈值
        Assert.assertEquals(listOf(6L to 10L), events)

        input.read(buffer) // 累计 9 字节，增量不足阈值
        Assert.assertEquals(listOf(6L to 10L), events)

        input.read(buffer) // 累计 10 字节，再次达到阈值
        Assert.assertEquals(listOf(6L to 10L, 10L to 10L), events)

        input.close() // 无尾部增量，不应重复回调
        Assert.assertEquals(2, events.size)
        Assert.assertEquals(10L, input.bytesRead)
    }

    @Test
    fun inputStream_monitorTailOnClose_test() {
        var count = 0
        val input = MonitorableInputStream(ByteArrayInputStream(ByteArray(10)), totalBytes = 10, threshold = 4) { _, _ ->
            count++
        }

        input.read(ByteArray(3)) // 3 字节，未达到阈值
        Assert.assertEquals(0, count)

        input.close() // 关闭时补发一次尾部回调
        Assert.assertEquals(1, count)

        input.close() // 幂等，不重复补发
        Assert.assertEquals(1, count)
    }

    @Test
    fun outputStream_monitorAtThreshold_test() {
        val events = mutableListOf<Pair<Long, Long>>()
        val output = MonitorableOutputStream(ByteArrayOutputStream(), totalBytes = 10, threshold = 4) { written, total ->
            events += written to total
        }

        output.write(ByteArray(3))
        Assert.assertTrue(events.isEmpty()) // 3 字节，未达到阈值

        output.write(ByteArray(3))
        Assert.assertEquals(listOf(6L to 10L), events) // 累计 6 字节，达到阈值

        output.write(ByteArray(3))
        Assert.assertEquals(listOf(6L to 10L), events) // 累计 9 字节，增量不足阈值

        output.write(ByteArray(3))
        Assert.assertEquals(listOf(6L to 10L, 12L to 10L), events) // 累计 12 字节，再次达到阈值

        output.close() // 无尾部增量，不应重复回调
        Assert.assertEquals(2, events.size)
        Assert.assertEquals(12L, output.bytesWritten)
    }

    @Test
    fun outputStream_monitorTailOnClose_test() {
        var count = 0
        val output = MonitorableOutputStream(ByteArrayOutputStream(), totalBytes = 10, threshold = 4) { _, _ ->
            count++
        }

        output.write(3)
        Assert.assertEquals(0, count)

        output.close() // 关闭时补发一次尾部回调
        Assert.assertEquals(1, count)

        output.close() // 幂等，不重复补发
        Assert.assertEquals(1, count)
    }

    @Test
    fun copyStream_withOptions_test() {
        // 自定义缓冲区大小，并关闭取消检查
        val size = 1000
        val bytes = ByteArray(size) { it.toByte() }
        val input = ByteArrayInputStream(bytes)
        val output = ByteArrayOutputStream()

        val bytesCopied = IoService.copy(input, output, IoOptions(bufferSize = 7, cancellable = false))

        Assert.assertEquals(size.toLong(), bytesCopied)
        Assert.assertArrayEquals(bytes, output.toByteArray())
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
