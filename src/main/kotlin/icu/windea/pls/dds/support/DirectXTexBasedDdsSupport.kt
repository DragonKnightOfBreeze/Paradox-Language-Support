package icu.windea.pls.dds.support

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.io.*
import icu.windea.pls.dds.*

private val logger = logger<DirectXTexBasedDdsSupport>()

/**
 * 用于在Windows环境下，通过DirectXTex的Texconv工具，进行相关的图片处理操作，例如将DDS图片转化为PNG图片。
 *
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class DirectXTexBasedDdsSupport : DdsSupport {
    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        return null //unnecessary to implement
    }

    private val texconvExeSynchronizer by lazy {
        val path = PlsConstants.Paths.texconvExe
        val sourceFile = VfsUtil.findFileByURL(PlsConstants.Paths.texconvExeClasspathUrl) ?: return@lazy null
        FileSynchronizer(path, sourceFile)
    }
    private val texconvExe get() = texconvExeSynchronizer?.synced()
    private val texconvExeWd by lazy {
        val path = PlsConstants.Paths.texconvExe
        path.parent?.toFile()
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFormat: String): VirtualFile? {
        val exe = texconvExe?.name ?: return null
        val s = file.path.quote()
        val ft = targetFormat
        val o = targetDirectory.path.quote()
        val command = "$exe $s -ft $ft -o $o -y" // -y: overwrite existing files
        val wd = texconvExeWd

        val r = runCatchingCancelable {
            executeCommand(command, CommandType.CMD, workDirectory = wd)
        }.onFailure { e ->
            logger.warn(e.message, e)
            return null
        }.getOrThrow()
        val targetFilePath = r.lines().lastOrNull()?.removePrefix("writing ")?.trim() ?: return null
        val targetFile = VfsUtil.findFile(targetFilePath.toPath(), true)
        return targetFile
    }
}
