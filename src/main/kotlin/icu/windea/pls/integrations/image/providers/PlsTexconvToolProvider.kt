package icu.windea.pls.integrations.image.providers

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import org.apache.commons.io.file.*
import java.nio.file.*
import java.util.*
import kotlin.io.path.*

/**
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class PlsTexconvToolProvider : PlsCommandBasedImageToolProvider() {
    private val logger = thisLogger()

    private val texconvExe by lazy { PlsConstants.Paths.texconvExeFile }
    private val texconvExeWd by lazy { PlsConstants.Paths.texconvExe.parent?.toFile() }

    override fun isEnabled(): Boolean {
        return OS.isWindows
    }

    override fun validate(): Boolean {
        return true // built-in, no need to validate
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

        val exe = texconvExe.name
        val s = path.toString().quote()
        val o = outputDirectoryPath.toString().quote()
        val ft = targetFormat
        val command = "$exe $s -ft $ft -o $o -y" // -y: overwrite existing files
        val wd = texconvExeWd

        val r = executeCommand(command, CommandType.CMD, workDirectory = wd)
        val lines = r.lines()
        val outputPath = lines.firstNotNullOfOrNull { it.removePrefixOrNull("writing ")?.trim()?.toPathOrNull() }
        val hasWarnings = lines.any { it.startsWith("WARNING: ") }

        if (hasWarnings) {
            logger.warn("Execute texconv command with warnings.\nCommand: $command\nCommand result: $r")
        } else {
            logger.info("Execute texconv command.\nCommand: $command\nCommand result: $r")
        }

        if (outputPath == null) throw IllegalStateException()

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
