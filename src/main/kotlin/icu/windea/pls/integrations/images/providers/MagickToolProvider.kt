package icu.windea.pls.integrations.images.providers

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancellable
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.integrations.images.ImageToolSettings
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists

/**
 * 参见：[Image Magick](https://www.imagemagick.org)
 */
class MagickToolProvider : CommandBasedImageToolProvider() {
    override val name = "magick"

    override fun isEnabled(): Boolean {
        return ImageToolSettings.getInstance().enableMagick
    }

    override fun isSupported(): Boolean {
        return true
    }

    override fun getExecutablePath(): Path? {
        val configured = ImageToolSettings.getInstance().magickPath?.trim()?.orNull()
        configured?.toPathOrNull()?.let { return it }
        return null // by default null
    }

    override fun validateExecutablePath(path: Path): Boolean {
        // NOTE 3.0.5 出于轻量化的目的，不再通过执行命令行来检测工具是否可用
        return Files.isRegularFile(path)
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
        return runCatchingCancellable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }
            .onFailure { thisLogger().warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val executablePath = getExecutablePath() ?: throw IllegalStateException("ImageMagick path is not configured or invalid")

        val tempParentPath = ChronicleDataPathService.getInstance().imagesTempPath
        val outputDirectoryPath = targetDirectoryPath ?: tempParentPath
        outputDirectoryPath.createDirectories()
        val outputFileName = targetFileName ?: (path.nameWithoutExtension + "." + targetFormat)
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val commands = listOf(
            executablePath.toString(),
            path.toString(),
            outputPath.toString(),
        )
        val commandLine = GeneralCommandLine(commands)
            .withWorkingDirectory(executablePath.parent)
            .withCharset(Charsets.UTF_8)
        if (SystemInfo.isLinux && executablePath.toString().endsWith(".AppImage", ignoreCase = true)) {
            commandLine.withEnvironment("APPIMAGE_EXTRACT_AND_RUN", "1")
        }

        ProgressManager.checkCanceled()
        val output = ProcessExecutor(commandLine).withTimeout(ExecutionTimeouts.normal).execute()

        if (output.isCancelled) {
            ProgressManager.checkCanceled() // 如果的确被取消，则抛出 `ProcessCanceledException`
            val message = "Failed to convert image: command was cancelled." +
                "\nCommand: ${commandLine.commandLineString}"
            throw IllegalStateException(message)
        }
        if (output.isTimeout) {
            val message = "Failed to convert image: command timed out." +
                "\nCommand: ${commandLine.commandLineString}"
            throw IllegalStateException(message)
        }
        if (output.exitCode != 0) {
            val message = "Failed to convert image: command failed (exitCode=${output.exitCode})." +
                "\nCommand: ${commandLine.commandLineString}" +
                "\nError: ${output.stderr.trim()}"
            throw IllegalStateException(message)
        }
        if (outputPath.notExists()) {
            val message = "Failed to convert image: output file not found." +
                "\nCommand: ${commandLine.commandLineString}" +
                "\nResult: ${output.stdout.trim()}" +
                "\nError: ${output.stderr.trim()}"
            throw IllegalStateException(message)
        }

        return outputPath
    }
}
