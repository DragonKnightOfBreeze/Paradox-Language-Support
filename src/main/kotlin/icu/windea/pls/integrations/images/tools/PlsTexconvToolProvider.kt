package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.constants.*
import java.nio.file.*
import kotlin.io.path.*

/**
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
@WithOS(OS.Windows)
class PlsTexconvToolProvider : PlsCommandBasedImageToolProvider() {
    private val texconvExe by lazy { PlsPathConstants.texconvExeFile }
    private val texconvExeWd by lazy { PlsPathConstants.texconvExe.parent?.toFile() }

    override fun isEnabled(): Boolean {
        return PlsFacade.getIntegrationsSettings().image.enableTexconv
    }

    override fun isSupported(): Boolean {
        return OS.value == OS.Windows
    }

    override fun isValid(): Boolean {
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
        val tempParentPath = PlsPathConstants.imagesTemp
        val outputDirectoryPath = targetDirectoryPath ?: tempParentPath
        val outputFileName = path.nameWithoutExtension + "." + targetFormat
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        outputDirectoryPath.createDirectories()

        val wd = texconvExeWd
        val exe = texconvExe.name.quoteIfNecessary('\'')
        val input = path.toString().quote('\'')
        val output = outputDirectoryPath.toString().quote('\'')

        val command = "./$exe $input -o $output -ft $targetFormat -y" // -y: overwrite existing files
        val result = executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件
        val lines = result.lines()
        val hasWarnings = lines.any { it.startsWith("WARNING: ") }

        if (hasWarnings) {
            thisLogger().warn("Execute texconv command with warnings.\nCommand: $command\nCommand result: $result")
        } else {
            thisLogger().info("Execute texconv command.\nCommand: $command\nCommand result: $result")
        }

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
