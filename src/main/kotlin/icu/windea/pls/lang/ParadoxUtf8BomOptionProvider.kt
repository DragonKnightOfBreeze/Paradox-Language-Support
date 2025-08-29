package icu.windea.pls.lang

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.encoding.Utf8BomOptionProvider
import icu.windea.pls.core.matchesPath
import icu.windea.pls.model.ParadoxFileType

/**
 * 基于文件的扩展名以及相对于入口目录的路径，判断创建新的脚本文件或本地化文件时，是否需要添加BOM。
 */
class ParadoxUtf8BomOptionProvider : Utf8BomOptionProvider {
    override fun shouldAddBOMForNewUtf8File(file: VirtualFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return when (fileInfo.fileType) {
            ParadoxFileType.Script -> "common/name_lists".matchesPath(fileInfo.path.parent)
            ParadoxFileType.Localisation -> true
            else -> false
        }
    }
}
