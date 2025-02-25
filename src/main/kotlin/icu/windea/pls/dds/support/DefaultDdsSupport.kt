package icu.windea.pls.dds.support

import com.intellij.openapi.vfs.*
import io.github.ititus.dds.*

/**
 * 用于获取DDS图片的元数据，以及在没有更好的方案的情况下渲染与转化DDS图片。
 *
 * 参见：[GitHub: iTitus/dds](https://github.com/iTitus/dds)
 */
class DefaultDdsSupport: DdsSupport {
    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        val ddsFile = DdsFile.load(file.toNioPath())
        if(ddsFile == null) return null
        val ddsMetadata = DdsMetadata(
            width = ddsFile.width(),
            height = ddsFile.height(),
            hasMipMaps = ddsFile.hasMipmaps(),
            isFlatTexture = ddsFile.isFlatTexture,
            isCubeMap = ddsFile.isCubemap,
            isVolumeTexture = ddsFile.isVolumeTexture,
            isDxt10 = ddsFile.isDxt10,
            d3dFormat = ddsFile.d3dFormat()?.toString(),
            dxgiFormat = ddsFile.dxgiFormat()?.toString(),
        )
        return ddsMetadata
    }
}
