package icu.windea.pls.core.util.net

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 下载选项。
 *
 * @property maxRetries 失败后允许的最大重试次数（不包含首次尝试）。
 * @property retryDelay 重试前的等待时间基数（实际等待时间会随重试次数线性增长）。
 */
data class DownloadOptions(
    val connectTimeout: Duration = 15.seconds,
    val requestTimeout: Duration = 30.seconds,
    val minSpeedCheckDelay: Duration = 10.seconds,
    val minSpeedBytesPerSecond: Long = 32L * 1024,
    val stallTimeout: Duration = 60.seconds,
    val maxDuration: Duration = 10.minutes,
    val userAgent: String = "Paradox-Chronicle",
    val maxRetries: Int = 3,
    val retryDelay: Duration = 1.seconds,
) {
    companion object {
        @JvmField val DEFAULT = DownloadOptions()
    }
}
