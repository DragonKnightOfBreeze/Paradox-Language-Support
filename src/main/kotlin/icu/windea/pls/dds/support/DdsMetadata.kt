package icu.windea.pls.dds.support

data class DdsMetadata(
    val width: Int,
    val height: Int,
    val hasMipMaps: Boolean? = null,
    val isFlatTexture: Boolean? = null,
    val isCubeMap: Boolean? = null,
    val isVolumeTexture: Boolean? = null,
    val isDxt10: Boolean? = null,
    val d3dFormat: String? = null,
    val dxgiFormat: String? = null,
)
