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
 * 参见：[Paint.NET](https://www.paint.net)
 */
@WithOS(OS.Windows)
class PlsPaintNetToolProvider : PlsCommandBasedImageToolProvider() {
    private val logger = thisLogger()

    override fun isEnabled(): Boolean {
        val settings = PlsFacade.getIntegrationsSettings().image
        return settings.enablePaintNet && settings.paintNetPath.isNotNullOrEmpty()
    }

    override fun isAvailable(): Boolean {
        return OS.isWindows
    }

    override fun validate(): Boolean {
        val settings = PlsFacade.getIntegrationsSettings().image
        val paintNetPath = settings.paintNetPath
        if (paintNetPath.isNullOrEmpty()) return false
        return runCatchingCancelable { doValidate(paintNetPath) }.getOrDefault(false)
    }

    private fun doValidate(paintNetPath: String): Boolean {
        val exe = paintNetPath.quote()
        val command = "$exe /?"
        val result = executeCommand(command, CommandType.CMD)
        // Paint.NET 命令行会输出帮助信息，简单判断有输出即可
        return result.isNotBlank()
    }

    // 常用命令示例（Paint.NET）：
    // 1. 图片格式转换：
    //    paintdotnet.exe /autoheadless /open input.png /saveas output.jpg
    //    （自动根据扩展名转换格式，支持常见图片格式）
    // 2. 批量处理（可多次使用/open和/saveas参数）：
    //    paintdotnet.exe /autoheadless /open input1.png /saveas out1.jpg /open input2.png /saveas out2.jpg
    // 3. 其它命令可参考官方文档：https://www.getpaint.net/doc/latest/CommandLine.html

    override fun open(file: VirtualFile): Boolean {
        TODO("Not yet implemented")
    }

    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        return runCatchingCancelable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }.onFailure { thisLogger().warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val settings = PlsFacade.getIntegrationsSettings().image
        val paintNetPath = settings.paintNetPath!!
        val tempParentPath = PlsConstants.Paths.imagesTemp
        val outputDirectoryPath = tempParentPath.resolve(UUID.randomUUID().toString())
        outputDirectoryPath.createDirectories()

        val exe = paintNetPath.quote()
        val inputPath = path.toString().quote()
        val outputFileName = targetFileName ?: (path.nameWithoutExtension + "." + targetFormat)
        val outputPath = outputDirectoryPath.resolve(outputFileName)
        val outputPathStr = outputPath.toString().quote()
        // Paint.NET 支持 /autoheadless /open /saveas 参数进行批处理
        val command = "$exe /autoheadless /open $inputPath /saveas $outputPathStr"
        val wd = paintNetPath.toPathOrNull()?.parent?.toFile()

        val result = executeCommand(command, CommandType.CMD, workDirectory = wd)

        thisLogger().info("Execute paint.net command.\nCommand: $command\nCommand result: $result")

        if (!outputPath.exists()) {
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
