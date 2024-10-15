package icu.windea.pls.lang.util.image

import co.phoenixlab.dds.*
import java.io.*

object ImageManager {
    //可选方案：
    //DDS4J (https://github.com/vincentzhang96/DDS4J)
    //JOGL (https://jogamp.org/jogl/www/)
    //OPENRNDR (https://github.com/openrndr/openrndr/tree/master/openrndr-dds)

    //目前选用：
    //DDS4J - 简单易用

    private val ddsImageDecoder: DdsImageDecoder by lazy { DdsImageDecoder() }

    fun convertDdsToPng(inputStream: InputStream, frameInfo: FrameInfo? = null): ByteArray? {
        val dds = Dds()
        dds.read(inputStream)
        return ddsImageDecoder.convertToPNG(dds, frameInfo)
    }

    fun convertDdsToPng(inputStream: InputStream, outputStream: OutputStream, frameInfo: FrameInfo? = null) {
        val dds = Dds()
        dds.read(inputStream)
        ddsImageDecoder.convertToPNG(dds, outputStream, frameInfo)
    }
}

