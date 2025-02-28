package icu.windea.pls.dds.support

import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.dds.*
import io.github.ititus.dds.*
import java.io.*
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import javax.imageio.*

/**
 * 用于获取DDS图片的元数据，以及在没有更好的方案的情况下渲染与转化DDS图片。
 *
 * 参见：[GitHub: iTitus/dds](https://github.com/iTitus/dds)
 */
class DefaultDdsSupport : DdsSupport {
    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        val ddsFile = DdsFile.load(file.toNioPath())
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

    override fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader? {
        return null //unnecessary to implement
    }

    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): OutputStream {
        try {
            return doConvertImageFormat(inputStream, outputStream, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        }
    }

    override fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFileName: String, sourceFormat: String, targetFormat: String): VirtualFile? {
        try {
            val path = file.toNioPath()
            val inputStream = Files.newInputStream(path, READ)
            val newFileName = targetFileName
            val newPath = targetDirectory.toNioPath().resolve(newFileName)
            val outputStream by lazy { Files.newOutputStream(newPath, WRITE, CREATE, TRUNCATE_EXISTING) }
            doConvertImageFormat(inputStream, outputStream, targetFormat)
            return VfsUtil.findFile(newPath, true)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun doConvertImageFormat(inputStream: InputStream, outputStream: OutputStream, targetFormat: String): OutputStream {
        inputStream.use { inputStream ->
            val imageInputStream = ImageIO.createImageInputStream(inputStream)
            val imageReader = ImageIO.getImageReaders(imageInputStream).next()
            imageReader.input = imageInputStream
            val numImages = imageReader.getNumImages(false)
            if (numImages == 0) throw IllegalStateException()
            repeat(numImages) { i ->
                val image0 = imageReader.read(i)
                outputStream.use { outputStream ->
                    ImageIO.write(image0, targetFormat, outputStream)
                    outputStream.flush()
                }
            }
        }
        return outputStream
    }
}
