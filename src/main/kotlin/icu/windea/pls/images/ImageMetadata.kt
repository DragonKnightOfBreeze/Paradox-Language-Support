package icu.windea.pls.images

/**
 * 图片的原始元数据。
 *
 * @property width 原始宽度。
 * @property height 原始高度。
 * @property format 资源格式（如 `DDS` 的像素格式）。
 * @property bpp 每个像素占用的位数（bits per pixel）。
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val format: String? = null,
    val bpp: Int? = null,
)
