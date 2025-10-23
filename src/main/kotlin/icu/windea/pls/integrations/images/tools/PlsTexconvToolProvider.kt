package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.quote
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.util.OS
import icu.windea.pls.lang.annotations.WithOS
import icu.windea.pls.model.constants.PlsPathConstants
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists

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

    override fun validatePath(path: String): Boolean {
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
        outputDirectoryPath.createDirectories()
        val outputFileName = path.nameWithoutExtension + "." + targetFormat
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val wd = texconvExeWd
        val exe = texconvExe.name.quoteIfNecessary('\'')
        val input = path.toString().quote('\'')
        val output = outputDirectoryPath.toString().quote('\'')

        val command = "./$exe $input -o $output -ft $targetFormat -y" // -y: overwrite existing files
        val result = executeCommand(command, workDirectory = wd) // 尽可能地先转到工作目录，再执行可执行文件

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
