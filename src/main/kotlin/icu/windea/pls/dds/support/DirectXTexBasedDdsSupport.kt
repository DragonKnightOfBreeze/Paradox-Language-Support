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
        val file = PlsConstants.Paths.texconvExe.toVirtualFile(false) ?: return@lazy null
        val sourceFile = VfsUtil.findFileByURL(PlsConstants.Paths.texconvExeClasspathUrl) ?: return@lazy null
        FileSynchronizer(file, sourceFile)
    }

    /**
     * @param targetFormat 参见：[File and pixel format options](https://github.com/microsoft/DirectXTex/wiki/Texconv#file-and-pixel-format-options)
     */
    override fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFormat: String): VirtualFile? {
        val texconvExe = texconvExeSynchronizer?.synced() ?: return null
        val r = runCatchingCancelable {
            val command = "${texconvExe.path} \"${file.path}\" -ft ${targetFormat} -o ${targetDirectory.path}"
            executeCommand(command, CommandType.POWER_SHELL)
        }.onFailure { e ->
            logger.warn(e.message, e)
            return null
        }.getOrThrow()
        val targetFilePath = r.lines().lastOrNull()?.removePrefix("writing ") ?: return null
        val targetFile = VfsUtil.findFile(targetFilePath.toPath(), true)
        return targetFile
    }
}
