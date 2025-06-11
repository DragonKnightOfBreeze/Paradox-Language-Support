package icu.windea.pls.dds.support

import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import io.github.ititus.dds.*
import io.github.ititus.ddsiio.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import javax.imageio.*
import kotlin.io.path.*

/**
 * 用于获取DDS图片的元数据，以及在没有更好的方案的情况下渲染与转化DDS图片。
 *
 * 参见：[GitHub: iTitus/dds](https://github.com/iTitus/dds)
 */
class DefaultDdsSupport : DdsSupport {
    override fun getMetadata(file: VirtualFile): DdsMetadata? {
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

    override fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader {
        return ImageReader(spi, this)
    }

    class ImageReader(
        spi: DdsImageReaderSpi,
        private val support: DefaultDdsSupport,
    ) : DdsImageReader(spi) {
        override fun read(imageIndex: Int, param: ImageReadParam?): BufferedImage? {
            return doRead(imageIndex, param)
        }

        private fun doRead(imageIndex: Int, param: ImageReadParam?): BufferedImage? {
            return super.read(imageIndex, param)
        }
    }

    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        try {
            doConvertImageFormat(inputStream, outputStream, targetFormat)
            return true
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        }
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        try {
            val inputStream = path.inputStream(READ)
            val outputStream = targetPath.outputStream(WRITE, CREATE, TRUNCATE_EXISTING)
            doConvertImageFormat(inputStream, outputStream, targetFormat)
            return targetPath.exists()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun doConvertImageFormat(inputStream: InputStream, outputStream: OutputStream, targetFormat: String): OutputStream {
        inputStream.buffered().use { inputStream ->
            val imageInputStream = ImageIO.createImageInputStream(inputStream)
            val imageReader = ImageIO.getImageReaders(imageInputStream).next()
            imageReader.input = imageInputStream
            val numImages = imageReader.getNumImages(false)
            if (numImages == 0) throw IllegalStateException()
            outputStream.use { outputStream ->
                repeat(numImages) { i ->
                    val image0 = imageReader.read(i)
                    ImageIO.write(image0, targetFormat, outputStream)
                    outputStream.flush()
                }
            }
        }
        return outputStream
    }
}
