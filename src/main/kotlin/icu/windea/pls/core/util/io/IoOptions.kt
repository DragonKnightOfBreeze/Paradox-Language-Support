package icu.windea.pls.core.util.io

/**
 * IO 选项。
 *
 * 纯不可变的配置对象，不包含任何回调或进度对象。
 *
 * @property bufferSize 复制流时使用的缓冲区大小（字节）。
 * @property cancellable 是否在读取过程中进行取消检查。
 * @property cancellationThreshold 进行取消检查的字节阈值。
 */
data class IoOptions(
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    val cancellable: Boolean = true,
    val cancellationThreshold: Long = IO_CANCELLATION_THRESHOLD,
) {
    companion object {
        @JvmField val DEFAULT = IoOptions()
    }
}
