package icu.windea.pls.core.util.io

import java.io.FilterInputStream
import java.io.InputStream

/**
 * 可监视读取进度的 [InputStream]。
 *
 * 在读取的字节数增量达到阈值时，以及关闭时仍有未触发的尾部增量时，调用 [listener]。
 *
 * @property totalBytes 预期读取的总字节数，未知时为 `-1`。
 * @property threshold 触发 [listener] 的字节增量阈值。
 * @property listener 读取进度回调，参数依次为已读取字节数和预期总字节数。
 */
class MonitorableInputStream(
    inputStream: InputStream,
    val totalBytes: Long = -1L,
    private val threshold: Long = IO_MONITOR_THRESHOLD,
    private val listener: (bytesRead: Long, totalBytes: Long) -> Unit,
) : FilterInputStream(inputStream) {
    /** 已读取的字节数。 */
    var bytesRead: Long = 0L
        private set

    private var reportedBytes = 0L
    private var closed = false

    override fun read(): Int {
        val result = super.read()
        if (result >= 0) addBytes(1)
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = super.read(b, off, len)
        if (result > 0) addBytes(result.toLong())
        return result
    }

    override fun skip(n: Long): Long {
        val result = super.skip(n)
        if (result > 0) addBytes(result)
        return result
    }

    override fun close() {
        // 保证幂等性，避免重复补发最终回调或重复关闭底层流
        if (closed) return
        closed = true
        try {
            super.close()
        } finally {
            if (bytesRead > 0 && bytesRead > reportedBytes) {
                reportedBytes = bytesRead
                listener(bytesRead, totalBytes)
            }
        }
    }

    private fun addBytes(count: Long) {
        bytesRead += count
        if (bytesRead - reportedBytes >= threshold.coerceAtLeast(1)) {
            reportedBytes = bytesRead
            listener(bytesRead, totalBytes)
        }
    }
}
