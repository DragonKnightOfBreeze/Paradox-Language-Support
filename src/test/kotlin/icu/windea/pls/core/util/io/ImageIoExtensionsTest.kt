package icu.windea.pls.core.util.io

import com.intellij.openapi.progress.ProcessCanceledException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * @see ProgressCheckingInputStream
 * @see ProgressCheckingOutputStream
 * @see readImage
 * @see writeImage
 */
@RunWith(JUnit4::class)
class ImageIoExtensionsTest {
    @Test
    fun inputStream_checksCancellationAfterThreshold_test() {
        var checkedCount = 0
        val input = ProgressCheckingInputStream(ByteArrayInputStream(ByteArray(10)), threshold = 4) { checkedCount++ }
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
        val input = ProgressCheckingInputStream(ByteArrayInputStream(ByteArray(10)), threshold = 4) {
            throw ProcessCanceledException()
        }

        Assert.assertThrows(ProcessCanceledException::class.java) {
            input.read(ByteArray(4))
        }
    }

    @Test
    fun outputStream_checksCancellationAfterThreshold_test() {
        var checkedCount = 0
        val output = ProgressCheckingOutputStream(ByteArrayOutputStream(), threshold = 4) { checkedCount++ }

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
        val output = ProgressCheckingOutputStream(ByteArrayOutputStream(), threshold = 4) {
            throw ProcessCanceledException()
        }

        Assert.assertThrows(ProcessCanceledException::class.java) {
            output.write(ByteArray(4))
        }
    }

    @Test
    fun copyImageStream_withProgressChecking_test() {
        // 默认阈值的复制路径仍应正确复制全部字节
        val size = IMAGE_IO_CANCELLATION_CHECK_THRESHOLD.toInt() * 2
        val bytes = ByteArray(size) { it.toByte() }
        val input = ByteArrayInputStream(bytes)
        val output = ByteArrayOutputStream()

        copyImageStream(input, output)

        Assert.assertEquals(size, output.size())
        Assert.assertArrayEquals(bytes, output.toByteArray())
    }
}
