package icu.windea.pls.dds.support

import co.phoenixlab.dds.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.dds.*
import icu.windea.pls.model.*
import java.io.*

object DdsManager {
    fun getMetadata(file: VirtualFile): DdsMetadata? {
        return DdsSupport.EP_NAME.extensionList.firstNotNullOfOrNull { it.getMetadata(file) }
    }

    fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFormat: String): VirtualFile? {
        return DdsSupport.EP_NAME.extensionList.firstNotNullOfOrNull { it.convertImageFormat(file, targetDirectory, targetFormat) }
    }

    //可选方案：
    //DDS4J (https://github.com/vincentzhang96/DDS4J)
    //JOGL (https://jogamp.org/jogl/www/)
    //OPENRNDR (https://github.com/openrndr/openrndr/tree/master/openrndr-dds)

    //目前选用：
    //DDS4J - 简单易用

    private val ddsImageDecoder: DdsImageDecoder by lazy { DdsImageDecoder() }

    fun convertDdsToPng(inputStream: InputStream, frameInfo: ImageFrameInfo? = null): ByteArray? {
        val dds = Dds()
        dds.read(inputStream)
        return ddsImageDecoder.convertToPNG(dds, frameInfo)
    }

    fun convertDdsToPng(inputStream: InputStream, outputStream: OutputStream, frameInfo: ImageFrameInfo? = null) {
        val dds = Dds()
        dds.read(inputStream)
        ddsImageDecoder.convertToPNG(dds, outputStream, frameInfo)
    }
}
