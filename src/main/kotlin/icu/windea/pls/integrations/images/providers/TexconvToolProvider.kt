package icu.windea.pls.integrations.images.providers

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.integrations.images.ImageToolSettings
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists

/**
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class TexconvToolProvider : CommandBasedImageToolProvider() {
    override val name = "texconv"

    override fun isEnabled(): Boolean {
        return ImageToolSettings.getInstance().enableTexconv
    }

    override fun isSupported(): Boolean {
        return SystemInfo.isWindows // Windows only
    }

    override fun getExecutablePath(): Path {
        val configured = ImageToolSettings.getInstance().texconvPath?.trim()?.orNull()
        configured?.toPathOrNull()?.let { return it }
        return ChronicleDataPathService.getInstance().texconvExePath // actual default value
    }

    override fun validateExecutablePath(path: Path): Boolean {
        // NOTE 3.0.5 出于轻量化的目的，仅检测路径是否是常规文件，不再通过执行命令行来检测工具是否可用
        if (!path.nameWithoutExtension.equals(name, ignoreCase = true)) return false
        return Files.isRegularFile(path)
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        return runCatchingCancelable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }
            .onFailure { thisLogger().warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val executablePath = getExecutablePath()

        val tempParentPath = ChronicleDataPathService.getInstance().imagesTempPath
        val outputDirectoryPath = targetDirectoryPath ?: tempParentPath
        outputDirectoryPath.createDirectories()
        val outputFileName = path.nameWithoutExtension + "." + targetFormat
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val commands = listOf(
            executablePath.toString(),
            path.toString(),
            "-o", outputDirectoryPath.toString(),
            "-ft", targetFormat,
            "-y", // overwrite existing files
        )
        val commandLine = GeneralCommandLine(commands)
            .withWorkingDirectory(executablePath.parent)
            .withCharset(Charsets.UTF_8)

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

        val finalOutputPath = outputDirectoryPath.resolve(targetFileName ?: outputFileName)
        if (finalOutputPath != outputPath) {
            Files.move(outputPath, finalOutputPath, StandardCopyOption.REPLACE_EXISTING)
        }
        return finalOutputPath
    }
}
