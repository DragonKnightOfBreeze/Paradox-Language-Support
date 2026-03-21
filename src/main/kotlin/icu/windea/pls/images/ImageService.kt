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
import icu.windea.pls.model.constants.PlsConstants
import org.intellij.images.fileTypes.impl.ImageFileType
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.spi.IIORegistry

@Service
class ImageService : AppLifecycleListener, DynamicPluginListener {
    // 对于 DDS 和 TGA 图片，统一使用 TwelveMonkeys 提供的 SPI
    // 参见：https://github.com/haraldk/TwelveMonkeys

    private val ddsImageReaderSpi = DdsImageReaderSpi()
    private val tgaImageReaderSpi = TgaImageReaderSpi()

    private fun registerImageIOSpi() {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageReaderSpi)
    }

    private fun deregisterImageIOSpi() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageReaderSpi)
    }

    override fun appFrameCreated(commandLineArgs: List<String?>) {
        registerImageIOSpi()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId != PlsConstants.pluginId) return
        registerImageIOSpi()
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId != PlsConstants.pluginId) return
        deregisterImageIOSpi()
    }

    fun isImageFileType(fileType: FileType): Boolean {
        return fileType is ImageFileType || isExtendedImageFileType(fileType)
    }

    fun isExtendedImageFileType(fileType: FileType): Boolean {
        return fileType is DdsFileType || fileType is TgaFileType
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        val r = ImageSupport.EP_NAME.extensionList.any {
            it.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        val r = ImageSupport.EP_NAME.extensionList.any {
            it.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }

    fun sliceImage(image: BufferedImage, frameInfo: ImageFrameInfo): BufferedImage? {
        if (!frameInfo.canApply()) return null
        val width = image.width
        val height = image.height
        val finalFrames = if (frameInfo.frames > 0) frameInfo.frames else width / height
        val finalFrame = if (frameInfo.frame > finalFrames) finalFrames else frameInfo.frame
        val frameWidth = width / finalFrames
        val startX = (finalFrame - 1) * frameWidth
        return image.getSubimage(startX, 0, frameWidth, height)
    }

    companion object {
        @JvmStatic
        fun getInstance(): ImageService = service()
    }
}
