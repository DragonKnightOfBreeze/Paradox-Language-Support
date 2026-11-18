package icu.windea.pls.core.util.io

import java.io.FilterOutputStream
import java.io.OutputStream

/**
 * 可监视写入进度的 [OutputStream]。
 *
 * 在写入的字节数增量达到阈值时，以及关闭时仍有未触发的尾部增量时，调用 [listener]。
 *
 * @property totalBytes 预期写入的总字节数，未知时为 `-1`。
 * @property threshold 触发 [listener] 的字节增量阈值。
 * @property listener 写入进度回调，参数依次为已写入字节数和预期总字节数。
 */
class MonitorableOutputStream(
    outputStream: OutputStream,
    val totalBytes: Long = -1L,
    private val threshold: Long = IO_MONITOR_THRESHOLD,
    private val listener: (bytesWritten: Long, totalBytes: Long) -> Unit,
) : FilterOutputStream(outputStream) {
    /** 已写入的字节数。 */
    var bytesWritten: Long = 0L
        private set

    private var reportedBytes = 0L
    private var closed = false

    override fun write(b: Int) {
        out.write(b)
        addBytes(1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        addBytes(len.toLong())
    }

    override fun close() {
        // 保证幂等性，避免重复补发最终回调或重复关闭底层流
        if (closed) return
        closed = true
        try {
            super.close()
        } finally {
            if (bytesWritten > 0 && bytesWritten > reportedBytes) {
                reportedBytes = bytesWritten
                listener(bytesWritten, totalBytes)
            }
        }
    }

    private fun addBytes(count: Long) {
        bytesWritten += count
        if (bytesWritten - reportedBytes >= threshold.coerceAtLeast(1)) {
            reportedBytes = bytesWritten
            listener(bytesWritten, totalBytes)
        }
    }
}
