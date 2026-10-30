package icu.windea.pls.core.util.io

import com.intellij.openapi.progress.ProgressManager
import java.io.FilterInputStream
import java.io.InputStream

/**
 * 可取消的 [InputStream]。
 *
 * 在读取的字节数达到阈值时进行取消检查。
 */
class CancellableInputStream(
    inputStream: InputStream,
    private val threshold: Long = IO_CANCELLATION_THRESHOLD,
    private val checker: () -> Unit = { ProgressManager.checkCanceled() },
) : FilterInputStream(inputStream) {
    private var checkedBytes = 0L

    override fun read(): Int {
        val result = super.read()
        if (result >= 0) checkAfterRead(1)
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = super.read(b, off, len)
        if (result > 0) checkAfterRead(result.toLong())
        return result
    }

    override fun skip(n: Long): Long {
        val result = super.skip(n)
        if (result > 0) checkAfterRead(result)
        return result
    }

    private fun checkAfterRead(count: Long) {
        checkedBytes += count
        if (checkedBytes >= threshold.coerceAtLeast(1)) {
            checkedBytes = 0
            checker()
        }
    }
}
