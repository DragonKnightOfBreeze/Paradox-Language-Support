package icu.windea.pls.images

/**
 * 图片的原始元数据。
 *
 * 备注：目前只保留渲染缩放所需的宽高；像 DDS 像素格式、TGA bpp 等格式信息由对应的元数据模型自行维护。
 *
 * @property width 原始宽度。
 * @property height 原始高度。
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
)
