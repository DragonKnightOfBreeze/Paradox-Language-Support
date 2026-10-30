package icu.windea.pls.core.util.io

import com.intellij.openapi.progress.ProgressManager
import java.io.FilterOutputStream
import java.io.OutputStream

/**
 * 可取消的 [OutputStream]。
 *
 * 在写入的字节数达到阈值时进行取消检查。
 */
class CancellableOutputStream(
    outputStream: OutputStream,
    private val threshold: Long = IO_CANCELLATION_THRESHOLD,
    private val checker: () -> Unit = { ProgressManager.checkCanceled() },
) : FilterOutputStream(outputStream) {
    private var checkedBytes = 0L

    override fun write(b: Int) {
        out.write(b)
        checkAfterWrite(1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        checkAfterWrite(len.toLong())
    }

    private fun checkAfterWrite(count: Long) {
        checkedBytes += count
        if (checkedBytes >= threshold.coerceAtLeast(1)) {
            checkedBytes = 0
            checker()
        }
    }
}
