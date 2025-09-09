package icu.windea.pls.images.dds

/**
 * DDS 图片的元数据。
 *
 * @property width 宽度。
 * @property height 高度。
 * @property format 资源格式。
 *
 * @see DdsMetadataReader
 */
data class DdsMetadata(
    val width: Int,
    val height: Int,
    val format: String,
)
