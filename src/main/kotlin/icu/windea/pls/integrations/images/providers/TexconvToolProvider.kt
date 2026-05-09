package icu.windea.pls.integrations.images.providers

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.system.OS
import icu.windea.pls.core.executeCommandLine
import icu.windea.pls.core.quote
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.tools.PlsDataPathService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists

/**
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class TexconvToolProvider : CommandBasedImageToolProvider() {
    override fun isEnabled(): Boolean {
        return PlsIntegrationsSettings.getInstance().state.image.enableTexconv
    }

    override fun isSupported(): Boolean {
        return OS.CURRENT == OS.Windows
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
        val tempParentPath = PlsDataPathService.getInstance().imagesTempPath
        val outputDirectoryPath = targetDirectoryPath ?: tempParentPath
        outputDirectoryPath.createDirectories()
        val outputFileName = path.nameWithoutExtension + "." + targetFormat
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val exePath = PlsDataPathService.getInstance().texconvExePath
        val wd = exePath.parent?.toFile()
        val exe = exePath.name.quoteIfNecessary('\'')
        val input = path.toString().quote('\'')
        val output = outputDirectoryPath.toString().quote('\'')

        ProgressManager.checkCanceled() // 在执行命令前检查进度是否被取消

        val command = "./$exe $input -o $output -ft $targetFormat -y" // -y: overwrite existing files
        val result = executeCommandLine(command, workDirectory = wd) // 尽可能地先转到工作目录，再执行可执行文件

        if (outputPath.notExists()) {
            throw IllegalStateException("Failed to convert image: output file not found.\nCommand: $command\nResult: $result")
        }

        val finalOutputPath = outputDirectoryPath.resolve(targetFileName ?: outputFileName)
        if (finalOutputPath != outputPath) {
            Files.move(outputPath, finalOutputPath, StandardCopyOption.REPLACE_EXISTING)
        }
        return finalOutputPath
    }
}
