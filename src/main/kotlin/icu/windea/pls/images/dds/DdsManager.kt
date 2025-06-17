package icu.windea.pls.images.dds

import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import io.github.ititus.dds.*

object DdsManager {
    fun getMetadata(file: VirtualFile): DdsMetadata? {
        val ddsFile = runCatchingCancelable { DdsFile.load(file.toNioPath()) }.getOrNull()
        if (ddsFile == null) return null
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

    //通过 TwelveMonkeys + ImageIO 获取元数据的话，获取到的信息是不全面的

    //fun getMetadata(file: VirtualFile): DdsMetadata? {
    //    return readMetadata(ByteArrayInputStream(file.contentsToByteArray()))
    //}
    //
    //private fun readMetadata(input: Any): DdsMetadata? {
    //    ImageIO.setUseCache(false) //same as org.intellij.images.util.ImageInfoReader.read
    //    runCatchingCancelable {
    //        ImageIO.createImageInputStream(input).use { iis ->
    //            ImageIO.getImageReaders(iis).forEach { reader ->
    //                reader.setInput(iis, true)
    //                val width = reader.getWidth(0)
    //                val height = reader.getHeight(0)
    //                val it2 = reader.getImageTypes(0)
    //                val format = runCatchingCancelable {
    //                    //https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/javax/imageio/metadata/doc-files/standard_metadata.html
    //                    reader.getImageMetadata(0).getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName)?.castOrNull<IIOMetadataNode>()
    //                        ?.getElementsByTagName("Compression")?.item(0)?.castOrNull<IIOMetadataNode>()
    //                        ?.getElementsByTagName("CompressionTypeName")?.item(0)?.castOrNull<IIOMetadataNode>()
    //                        ?.getAttribute("value")
    //                }.getOrNull()
    //                return DdsMetadata(width, height, d3dFormat = format)
    //            }
    //        }
    //    }
    //    return null
    //}
}
