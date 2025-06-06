package icu.windea.pls.integrations.image.providers

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import org.apache.commons.io.file.*
import java.nio.file.*
import java.util.*
import kotlin.io.path.*

/**
 * 参见：[Image Magick](https://www.imagemagick.org)
 */
@WithOS(OS.Windows, OS.Linux)
class PlsMagickToolProvider : PlsCommandBasedImageToolProvider() {
    private val logger = thisLogger()

    override fun isEnabled(): Boolean {
        val settings = PlsFacade.getIntegrationsSettings().image
        return settings.enableMagick && settings.magickPath.isNotNullOrEmpty()
    }

    override fun validate(): Boolean {
        val settings = PlsFacade.getIntegrationsSettings().image
        val magickPath = settings.magickPath
        if (magickPath.isNullOrEmpty()) return false
        return runCatchingCancelable { doValidate(magickPath) }.getOrDefault(false)
    }

    private fun doValidate(magickPath: String): Boolean {
        val exe = magickPath.quote()
        val command = "$exe -version"
        val result = executeCommand(command)
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

    override fun open(file: VirtualFile): Boolean {
        TODO("Not yet implemented")
    }

    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        return runCatchingCancelable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }
            .onFailure { thisLogger().warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val settings = PlsFacade.getIntegrationsSettings().image
        val magickPath = settings.magickPath!!
        val tempParentPath = PlsConstants.Paths.imagesTemp
        val outputDirectoryPath = tempParentPath.resolve(UUID.randomUUID().toString())
        outputDirectoryPath.createDirectories()

        val exe = magickPath.quote()
        val inputPath = path.toString().quote()
        val outputFileName = targetFileName ?: (path.nameWithoutExtension + "." + targetFormat)
        val outputPath = outputDirectoryPath.resolve(outputFileName)
        val outputPathStr = outputPath.toString().quote()
        val command = "$exe $inputPath $outputPathStr"
        val wd = magickPath.toPathOrNull()?.parent?.toFile()

        val result = executeCommand(command, workDirectory = wd)

        thisLogger().info("Execute magick command.\nCommand: $command\nCommand result: $result")

        if (outputPath.notExists()) {
            throw IllegalStateException("Failed to convert image: output file not found.\nCommand: $command\nResult: $result")
        }

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
