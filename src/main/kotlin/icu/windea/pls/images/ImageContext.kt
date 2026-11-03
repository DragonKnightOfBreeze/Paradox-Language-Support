package icu.windea.pls.images

import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.dds.DdsImageReaderSpi
import icu.windea.pls.images.dds.DdsImageWriterSpi
import icu.windea.pls.images.tga.TgaFileType
import icu.windea.pls.images.tga.TgaImageReaderSpi
import icu.windea.pls.images.tga.TgaImageWriterSpi
import icu.windea.pls.inject.injectors.DDSImageReaderCodeInjector
import icu.windea.pls.inject.injectors.TGAImageReaderCodeInjector
import java.net.URLConnection
import javax.imageio.spi.IIORegistry

/**
 * @see DdsImageReaderSpi
 * @see DdsImageWriterSpi
 * @see TgaImageReaderSpi
 * @see TgaImageWriterSpi
 * @see DDSImageReaderCodeInjector
 * @see TGAImageReaderCodeInjector
 */
@PublishedApi
internal object ImageContext {
    // NOTE for DDS and TGA images, use SPIs from TwelveMonkeys
    // see: https://github.com/haraldk/TwelveMonkeys

    // TODO 3.0.5+ refactoring: built-in SPIs for DDS and TGA images
    // note: delay since it's complexity

    // NOTE 2.1.7 MUST be lazy loaded here (avoiding eager class loading of ImageReaders and make ImageReaderCodeInjectors work as expected)
    private val ddsImageReaderSpi by lazy { DdsImageReaderSpi() }
    private val ddsImageWriterSpi by lazy { DdsImageWriterSpi() }
    private val tgaImageReaderSpi by lazy { TgaImageReaderSpi() }
    private val tgaImageWriterSpi by lazy { TgaImageWriterSpi() }

    // NOTE 2.1.7 since IDEA 2026.1, it's necessary to set fileNameMap (or cannot render DDS / TGA images without splitting in quick doc as expected)
    private val originalFileNameMap by lazy { URLConnection.getFileNameMap() }

    @PublishedApi
    internal fun init() {
        registerImageIOSpi()
        setFileNameMap()
        ImageRenderService.getInstance().startCacheCleanup()
    }

    @PublishedApi
    internal fun cleanUp() {
        deregisterImageIOSpi()
        resetFileNameMap()
    }

    @PublishedApi
    internal fun registerImageIOSpi() {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageWriterSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageWriterSpi)
    }

    @PublishedApi
    internal fun deregisterImageIOSpi() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageWriterSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageWriterSpi)
    }

    @PublishedApi
    internal fun setFileNameMap() {
        // NOTE 2.1.7 lift out or will cause SOF
        val originalFileNameMap = originalFileNameMap
        URLConnection.setFileNameMap {
            val extension = it.substringAfterLast('.').lowercase()
            when (extension) {
                "dds" -> DdsFileType.contentTypeString
                "tga" -> TgaFileType.contentTypeString
                else -> originalFileNameMap.getContentTypeFor(it)
            }
        }
    }

    @PublishedApi
    internal fun resetFileNameMap() {
        URLConnection.setFileNameMap(originalFileNameMap)
    }
}

