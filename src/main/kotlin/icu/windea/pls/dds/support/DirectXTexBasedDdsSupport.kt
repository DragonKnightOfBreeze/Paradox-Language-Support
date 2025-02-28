package icu.windea.pls.dds.support

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.util.image.*
import io.github.ititus.ddsiio.*
import org.apache.commons.io.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import java.util.*
import javax.imageio.*
import javax.imageio.stream.*
import kotlin.io.path.*

/**
 * 用于在Windows环境下，通过DirectXTex的Texconv工具，进行相关的图片处理操作，例如将DDS图片转化为PNG图片。
 *
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class DirectXTexBasedDdsSupport : DdsSupport {
    private val texconvExe get() = PlsConstants.Paths.texconvExeFile
    private val texconvExeWd by lazy { PlsConstants.Paths.texconvExe.parent?.toFile() }
    private val tempParentPath get() = PlsConstants.Paths.images

    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        return null //unnecessary to implement
    }

    override fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader? {
        if (!OS.isWindows) return null //only available on windows
        return ImageReader(spi)
    }

    class ImageReader(spi: DdsImageReaderSpi) : DdsImageReader(spi) {
        val stream: ImageInputStream? by memberProperty("stream")

        override fun read(imageIndex: Int, param: ImageReadParam?): BufferedImage {
            val stream = stream ?: return super.read(imageIndex, param)
            val r = try {
                ImageManager.convertDdsToPng(stream)
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)
                null
            }
            return r ?: super.read(imageIndex, param)
        }
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): OutputStream? {
        try {
            val filePath = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            val targetDirectoryPath = tempParentPath
            val targetFilePath = doConvertImageFormat(filePath, targetDirectoryPath, null, targetFormat, useTemp = false) ?: return null
            IOUtils.copy(targetFilePath.inputStream(READ), outputStream)
            return outputStream
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFileName: String, sourceFormat: String, targetFormat: String): VirtualFile? {
        try {
            val filePath = file.toNioPath()
            val targetDirectoryPath = targetDirectory.toNioPath()
            val targetFilePath = doConvertImageFormat(filePath, targetDirectoryPath, targetFileName, targetFormat, useTemp = true) ?: return null
            return VfsUtil.findFile(targetFilePath, true)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    private fun doConvertImageFormat(filePath: Path, targetDirectoryPath: Path, targetFileName: String?, targetFormat: String, useTemp: Boolean): Path? {
        if (!OS.isWindows) return null //only available on windows

        val outputPath = if (useTemp) tempParentPath.resolve(UUID.randomUUID().toString()) else targetDirectoryPath
        outputPath.createDirectories()

        val exe = texconvExe.name
        val s = filePath.toString().quote()
        val o = outputPath.toString().quote()
        val ft = targetFormat
        val command = "$exe $s -ft $ft -o $o -y" // -y: overwrite existing files
        val wd = texconvExeWd

        val r = executeCommand(command, CommandType.CMD, workDirectory = wd)
        val outputFilePath = r.lines().lastOrNull()?.removePrefix("writing ")?.trim()?.toPathOrNull() ?: throw IllegalStateException()

        val targetFilePath = targetDirectoryPath.resolve(targetFileName ?: outputFilePath.name)
        if (targetFilePath != outputFilePath) {
            Files.move(outputFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING)
        }
        return targetFilePath
    }
}
