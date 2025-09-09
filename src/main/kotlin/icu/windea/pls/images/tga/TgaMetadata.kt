package icu.windea.pls.images.tga

/**
 * TAG 图片的元数据。
 *
 * @property width 宽度。
 * @property height 高度。
 * @property bpp 每个像素占用的位数（bits per pixel）。
 *
 * @see TgaMetadataReader
 */
data class TgaMetadata(
    val width: Int,
    val height: Int,
    val bpp: Int,
)
