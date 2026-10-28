package icu.windea.pls.images

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.spi.DdsImageReaderSpi
import icu.windea.pls.images.spi.TgaImageReaderSpi
import icu.windea.pls.images.tga.TgaFileType
import icu.windea.pls.model.constants.ChronicleConstants
import org.intellij.images.fileTypes.impl.ImageFileType
import java.io.InputStream
import java.io.OutputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.spi.IIORegistry

@Service
class ImageService : AppLifecycleListener, DynamicPluginListener {
    companion object {
        @JvmStatic
        fun getInstance(): ImageService = service()
    }

    // TODO 2.1.7+ refactoring: built-in image reader SPIs for DDS and TGA images
    // for DDS and TGA images, use image reader SPIs from TwelveMonkeys
    // see: https://github.com/haraldk/TwelveMonkeys

    // NOTE 2.1.7 MUST be lazy loaded here (avoiding eager class loading of ImageReaders and make ImageReaderCodeInjectors work as expected)
    private val ddsImageReaderSpi by lazy { DdsImageReaderSpi() }
    private val tgaImageReaderSpi by lazy { TgaImageReaderSpi() }

    // NOTE 2.1.7 since IDEA 2026.1, it's necessary to set fileNameMap (or cannot render DDS / TGA images without splitting in quick doc as expected)
    private val originalFileNameMap by lazy { URLConnection.getFileNameMap() }

    private fun registerImageIOSpi() {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageReaderSpi)
    }

    private fun deregisterImageIOSpi() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageReaderSpi)
    }

    private fun setFileNameMap() {
        val originalFileNameMap = originalFileNameMap // lift out or will cause SOF
        URLConnection.setFileNameMap {
            val extension = it.substringAfterLast('.').lowercase()
            when (extension) {
                "dds" -> DdsFileType.contentTypeString
                "tga" -> TgaFileType.contentTypeString
                else -> originalFileNameMap.getContentTypeFor(it)
            }
        }
    }

    private fun resetFileNameMap() {
        URLConnection.setFileNameMap(originalFileNameMap)
    }

    override fun appFrameCreated(commandLineArgs: List<String?>) {
        registerImageIOSpi()
        setFileNameMap()
        ImageRenderService.getInstance().startCacheCleanup()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        registerImageIOSpi()
        setFileNameMap()
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        deregisterImageIOSpi()
        resetFileNameMap()
    }

    fun isImageFileType(fileType: FileType): Boolean {
        return fileType is ImageFileType || isExtendedImageFileType(fileType)
    }

    fun isExtendedImageFileType(fileType: FileType): Boolean {
        return fileType is DdsFileType || fileType is TgaFileType
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        // 依次尝试各个实现；外部工具不支持某些格式组合时继续回退到内置实现
        var converted = false
        var lastError: UnsupportedOperationException? = null
        for (support in ImageSupport.EP_NAME.extensionList) {
            converted = try {
                support.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
            } catch (e: UnsupportedOperationException) {
                lastError = e
                false
            }
            if (converted) break
        }
        if (!converted) throw UnsupportedOperationException(lastError)
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        // 依次尝试各个实现；如果外部工具失败或产出空文件，则继续回退到内置实现
        var converted = false
        var lastError: UnsupportedOperationException? = null
        for (support in ImageSupport.EP_NAME.extensionList) {
            converted = try {
                support.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
            } catch (e: UnsupportedOperationException) {
                lastError = e
                false
            }
            if (converted && Files.isRegularFile(targetPath) && Files.size(targetPath) > 0) break
            runCatching { Files.deleteIfExists(targetPath) }
            converted = false
        }
        if (!converted) throw UnsupportedOperationException(lastError)
    }
}
