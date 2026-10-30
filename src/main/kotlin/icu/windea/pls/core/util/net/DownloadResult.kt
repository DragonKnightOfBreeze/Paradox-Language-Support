package icu.windea.pls.core.util.net

import java.nio.file.Path

/**
 * 下载结果。
 */
data class DownloadResult(
    val path: Path,
    val size: Long,
    val sha256: String,
)
