package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo

/**
 * 提供需要忽略的文件。
 *
 * 解析文件信息时，被忽略的文件不会被视为脚本文件、本地化文件或者 CSV 文件。其文件分组会被直接解析为 [ParadoxFileGroup.Other]。
 *
 * @see ParadoxFileGroup
 * @see ParadoxFileInfo
 */
interface ParadoxIgnoredFileProvider {
    fun isIgnoredFile(file: VirtualFile): Boolean

    fun isIgnoredFile(filePath: FilePath): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxIgnoredFileProvider>("icu.windea.pls.ignoredFileProvider")
    }
}
