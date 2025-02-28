package icu.windea.pls.lang.util.image

import co.phoenixlab.dds.*
import icu.windea.pls.dds.support.*
import icu.windea.pls.model.*
import java.awt.image.*
import java.io.*
import javax.imageio.stream.*

object ImageManager {
    //可选方案：
    //DDS4J (https://github.com/vincentzhang96/DDS4J)
    //JOGL (https://jogamp.org/jogl/www/)
    //OPENRNDR (https://github.com/openrndr/openrndr/tree/master/openrndr-dds)

    //目前选用：
    //DDS4J - 简单易用

    private val ddsImageDecoder: DdsImageDecoder by lazy { DdsImageDecoder() }

    @Throws(UnsupportedOperationException::class)
    fun convertDdsToPng(inputStream: InputStream, outputStream: OutputStream, frameInfo: ImageFrameInfo?) {
        if (frameInfo == null) {
            return convertDdsToPng(inputStream, outputStream)
        }
        //TODO 1.3.30
        val dds = Dds()
        dds.read(inputStream)
        ddsImageDecoder.convertToPNG(dds, outputStream, frameInfo)
    }

    @Throws(UnsupportedOperationException::class)
    fun convertDdsToPng(inputStream: InputStream, outputStream: OutputStream) {
        DdsSupport.convertImageFormat(inputStream, outputStream, "dds", "png") ?: throw UnsupportedOperationException()
    }

    @Throws(UnsupportedOperationException::class)
    fun convertDdsToPng(imageInputStream: ImageInputStream): BufferedImage? {
        //TODO 1.3.30
        return null
    }
}
