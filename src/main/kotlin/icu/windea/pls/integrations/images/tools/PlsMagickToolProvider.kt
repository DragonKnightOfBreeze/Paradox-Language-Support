package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.constants.*
import java.nio.file.*
import kotlin.io.path.*

/**
 * 参见：[Image Magick](https://www.imagemagick.org)
 */
@WithOS(OS.Windows, OS.Linux)
class PlsMagickToolProvider : PlsCommandBasedImageToolProvider() {
    override fun isEnabled(): Boolean {
        return PlsFacade.getIntegrationsSettings().image.enableMagick
    }

    override fun isSupported(): Boolean {
        return true
    }

    override fun isValid(): Boolean {
        val toolPath = PlsFacade.getIntegrationsSettings().image.magickPath?.trim()
        if (toolPath.isNullOrEmpty()) return false
        return validatePath(toolPath)
    }

    override fun validatePath(path: String): Boolean {
        return runCatchingCancelable { doValidatePath(path) }.getOrDefault(false)
    }

    private fun doValidatePath(path: String): Boolean {
        val fullExePath = path.toPath()
        val wd = fullExePath.parent?.toFile()
        val exe = fullExePath.name

        val command = "./$exe -version"
        val result = executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件
        return result.contains("ImageMagick") || result.contains("Version")
    }

    // 常用命令示例（ImageMagick）：
    // 1. 图片格式转换：
    //    magick input.png output.jpg
    //    （自动根据扩展名转换格式）
    // 2. 压缩图片（JPEG）：
    //    magick input.png -quality 85 output.jpg
    // 3. 缩放图片：
    //    magick input.png -resize 800x600 output.jpg
    // 4. 裁剪图片：
    //    magick input.png -crop 100x100+10+10 output.jpg
    // 5. 转为灰度图：
    //    magick input.png -colorspace Gray output.jpg
    // 6. 添加水印：
    //    magick input.jpg watermark.png -gravity southeast -geometry +10+10 -composite output.jpg

    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        return runCatchingCancelable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }
            .onFailure { thisLogger().warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val toolPath = PlsFacade.getIntegrationsSettings().image.magickPath?.trim()
        if (toolPath.isNullOrEmpty()) throw IllegalStateException()

        val tempParentPath = PlsPathConstants.imagesTemp
        val outputDirectoryPath = targetDirectoryPath ?: tempParentPath
        outputDirectoryPath.createDirectories()
        val outputFileName = targetFileName ?: (path.nameWithoutExtension + "." + targetFormat)
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val fullExePath = toolPath.toPath()
        val wd = fullExePath.parent?.toFile()
        val exe = fullExePath.name.quoteIfNecessary('\'')
        val input = path.toString().quote('\'')
        val output = outputPath.toString().quote('\'')

        val command = "./$exe $input $output"
        val result = executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件

        if (outputPath.notExists()) {
            throw IllegalStateException("Failed to convert image: output file not found.\nCommand: $command\nResult: $result")
        }

        return outputPath
    }
}
