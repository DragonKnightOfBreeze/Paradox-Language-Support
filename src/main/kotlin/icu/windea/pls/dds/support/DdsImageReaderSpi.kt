package icu.windea.pls.dds.support

import javax.imageio.*

/**
 * 用于为DDS图片提供对[ImageIO]的支持，例如直接从IDEA的编辑器中查看DDS图片。
 */
class DdsImageReaderSpi: io.github.ititus.ddsiio.DdsImageReaderSpi() {
    override fun createReaderInstance(extension: Any?): ImageReader {
        return DdsSupport.createImageReader(this, this) ?: super.createReaderInstance(extension)
    }
}
