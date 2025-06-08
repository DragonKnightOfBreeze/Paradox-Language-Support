package icu.windea.pls.integrations.image.tools

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import org.apache.commons.io.file.*
import java.nio.file.*
import java.util.*
import kotlin.io.path.*

/**
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
@WithOS(OS.Windows)
class PlsTexconvToolProvider : PlsCommandBasedImageToolProvider() {
    private val logger = thisLogger()

    private val texconvExe by lazy { PlsConstants.Paths.texconvExeFile }
    private val texconvExeWd by lazy { PlsConstants.Paths.texconvExe.parent?.toFile() }

    override fun isEnabled(): Boolean {
        return true // always true
    }

    override fun isAvailable(): Boolean {
        return OS.value == OS.Windows
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        return runCatchingCancelable { doConvertImageFormat(path, targetDirectoryPath, targetFileName, targetFormat) }
            .onFailure { logger.warn(it) }.getOrThrow()
    }

    private fun doConvertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, targetFormat: String): Path {
        val tempParentPath = PlsConstants.Paths.imagesTemp
        val outputDirectoryPath = tempParentPath.resolve(UUID.randomUUID().toString())
        outputDirectoryPath.createDirectories()
        val outputFileName = targetFileName ?: (path.nameWithoutExtension + "." + targetFormat)
        val outputPath = outputDirectoryPath.resolve(outputFileName)

        val wd = texconvExeWd
        val exe = texconvExe.name.quoteIfNecessary()
        val input = path.toString().quote()
        val output = outputDirectoryPath.toString().quote()

        val command = "./$exe $input -o $output -ft $targetFormat -y" // -y: overwrite existing files
        val result = executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件
        val lines = result.lines()
        val hasWarnings = lines.any { it.startsWith("WARNING: ") }

        if (hasWarnings) {
            logger.warn("Execute texconv command with warnings.\nCommand: $command\nCommand result: $result")
        } else {
            logger.info("Execute texconv command.\nCommand: $command\nCommand result: $result")
        }

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
