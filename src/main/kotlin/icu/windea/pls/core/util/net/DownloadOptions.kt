package icu.windea.pls.core.util.net

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 下载选项。
 */
data class DownloadOptions(
    val connectTimeout: Duration = 15.seconds,
    val requestTimeout: Duration = 30.seconds,
    val minSpeedCheckDelay: Duration = 10.seconds,
    val minSpeedBytesPerSecond: Long = 32L * 1024,
    val stallTimeout: Duration = 60.seconds,
    val maxDuration: Duration = 10.minutes,
    val userAgent: String = "Paradox-Chronicle",
) {
    companion object {
        @JvmField val DEFAULT = DownloadOptions()
    }
}
