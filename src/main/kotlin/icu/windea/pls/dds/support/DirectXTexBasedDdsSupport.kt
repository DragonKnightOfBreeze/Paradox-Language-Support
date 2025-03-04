package icu.windea.pls.dds.support

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.io.*
import icu.windea.pls.dds.*
import io.github.ititus.ddsiio.*
import org.apache.commons.io.*
import org.apache.commons.io.file.*
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

    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        return null //unnecessary to implement
    }

    override fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader? {
        if (!OS.isWindows) return null //only available on windows
        return ImageReader(spi, this)
    }

    class ImageReader(
        spi: DdsImageReaderSpi,
        private val support: DirectXTexBasedDdsSupport
    ) : DdsImageReader(spi) {
        val stream: ImageInputStream? by memberProperty<DdsImageReader, _>("stream")

        override fun read(imageIndex: Int, param: ImageReadParam?): BufferedImage {
            val stream = stream
            val image = try {
                doRead(stream)
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)
                null
            }
            return image ?: super.read(imageIndex, param)
        }

        private fun doRead(stream: ImageInputStream?): BufferedImage? {
            if (stream == null) return null
            val inputStream = ImageInputStreamAdapter(stream)
            val outputStream = ByteArrayOutputStream()
            val r = support.convertImageFormat(inputStream, outputStream, "dds", "png")
            if (!r) return null
            val input = ByteArrayInputStream(outputStream.toByteArray())
            return ImageIO.read(input)
        }
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        if (!OS.isWindows) return false //only available on windows
        try {
            val tempParentPath = PlsConstants.Paths.imagesTemp
            tempParentPath.createDirectories()
            val path = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            path.outputStream(WRITE, CREATE, TRUNCATE_EXISTING).use { IOUtils.copy(inputStream, it) }
            val targetPath = doConvertImageFormat(path, null, null, targetFormat)
            targetPath.inputStream(READ).use { IOUtils.copy(it, outputStream) }
            path.deleteIfExists()
            return true
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        if (!OS.isWindows) return false //only available on windows
        try {
            doConvertImageFormat(path, targetPath.parent, targetPath.name, targetFormat)
            return true
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val tempParentPath = PlsConstants.Paths.imagesTemp
        val outputDirectoryPath = tempParentPath.resolve(UUID.randomUUID().toString())
        outputDirectoryPath.createDirectories()

        val exe = texconvExe.name
        val s = path.toString().quote()
        val o = outputDirectoryPath.toString().quote()
        val ft = targetFormat
        val command = "$exe $s -ft $ft -o $o -y" // -y: overwrite existing files
        val wd = texconvExeWd

        val r = executeCommand(command, CommandType.CMD, workDirectory = wd)
        val outputPath = r.lines().lastOrNull()?.removePrefix("writing ")?.trim()?.toPathOrNull() ?: throw IllegalStateException()

        if (targetDirectoryPath == null) {
            if (targetFileName == null) return outputPath
            val targetPath = outputDirectoryPath.resolve(targetFileName)
            if (targetPath != outputPath) {
                Files.move(outputPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            return targetPath
        }
        val targetPath = targetDirectoryPath.resolve(targetFileName ?: outputPath.name)
        if (targetPath != outputPath) {
            Files.move(outputPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }
        if (targetDirectoryPath != outputDirectoryPath) {
            PathUtils.deleteDirectory(outputDirectoryPath)
        }
        return targetPath
    }
}
