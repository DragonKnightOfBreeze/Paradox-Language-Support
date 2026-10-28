package icu.windea.pls.images

import java.nio.file.Path

/**
 * 图片渲染请求。
 *
 * @property sourcePath 源图片路径。
 * @property targetPath 目标缓存图片路径。
 * @property frameInfo 帧数信息，用于切分图片。
 * @property maxWidth 目标最大宽度，为空时不限制。
 * @property maxHeight 目标最大高度，为空时不限制。
 */
data class ImageRenderRequest(
    val sourcePath: Path,
    val targetPath: Path,
    val frameInfo: ImageFrameInfo? = null,
    val maxWidth: Int? = null,
    val maxHeight: Int? = null,
) {
    val sourceFormat: String
        get() = sourcePath.fileName.toString().substringAfterLast('.', "").lowercase()

    val targetFormat: String
        get() = "png"
}
