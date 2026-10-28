package icu.windea.pls.integrations.images.providers

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProgressExecutor
import icu.windea.pls.core.runCatchingCancelable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.text.Charsets

/**
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class TexconvToolProvider : CommandBasedImageToolProvider() {
    override fun isEnabled(): Boolean {
        return ChronicleIntegrationsSettings.getInstance().state.image.enableTexconv
    }

    override fun isSupported(): Boolean {
        return SystemInfo.isWindows
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun isValidExePath(path: String): Boolean {
        return true
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        return runCatchingCancelable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }
            .onFailure { thisLogger().warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val tempParentPath = ChronicleDataPathService.getInstance().imagesTempPath
        val outputDirectoryPath = targetDirectoryPath ?: tempParentPath
        outputDirectoryPath.createDirectories()
        val outputFileName = path.nameWithoutExtension + "." + targetFormat
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val exePath = ChronicleDataPathService.getInstance().texconvExePath
        val commandLine = GeneralCommandLine(
            exePath.toString(),
            path.toString(),
            "-o", outputDirectoryPath.toString(),
            "-ft", targetFormat,
            "-y", // overwrite existing files
        ).withWorkingDirectory(exePath.parent).withCharset(Charsets.UTF_8)

        ProgressManager.checkCanceled()
        val output = ProgressExecutor(commandLine)
            .withTimeout(ExecutionTimeouts.normal)
            .execute()

        if (output.isCancelled) {
            ProgressManager.checkCanceled() // 如果的确被取消，则抛出 `ProcessCanceledException`
            throw IllegalStateException("Failed to convert image: command was cancelled.\nCommand: ${commandLine.commandLineString}")
        }
        if (output.isTimeout) {
            throw IllegalStateException("Failed to convert image: command timed out.\nCommand: ${commandLine.commandLineString}")
        }
        if (output.exitCode != 0) {
            throw IllegalStateException(
                "Failed to convert image: command failed (exitCode=${output.exitCode}).\n" +
                    "Command: ${commandLine.commandLineString}\n" +
                    "Error: ${output.stderr.trim()}"
            )
        }
        if (outputPath.notExists()) {
            throw IllegalStateException(
                "Failed to convert image: output file not found.\n" +
                    "Command: ${commandLine.commandLineString}\n" +
                    "Result: ${output.stdout.trim()}\n" +
                    "Error: ${output.stderr.trim()}"
            )
        }

        val finalOutputPath = outputDirectoryPath.resolve(targetFileName ?: outputFileName)
        if (finalOutputPath != outputPath) {
            Files.move(outputPath, finalOutputPath, StandardCopyOption.REPLACE_EXISTING)
        }
        return finalOutputPath
    }
}
