package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.ide.projectView.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import javax.swing.*

/**
 * 为游戏或模组目录，以及模组描述符文件，提供特殊图标。
 */
class ParadoxFileIconProvider : FileIconProvider, DumbAware {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        if (project == null) return null
        val fileInfo = file.fileInfo ?: return null
        if (file.isDirectory) {
            val rootInfo = fileInfo.rootInfo
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
            if (file != rootInfo.rootFile) return null
            if (ProjectRootsUtil.isModuleContentRoot(file, project)) return null
            if (ProjectRootsUtil.isModuleSourceRoot(file, project)) return null
            val icon = when (rootInfo) {
                is ParadoxRootInfo.Game -> PlsIcons.General.GameDirectory
                is ParadoxRootInfo.Mod -> PlsIcons.General.ModDirectory
                else -> null
            }
            return icon
        } else {
            val fileType = fileInfo.fileType
            if (fileType != ParadoxFileType.ModDescriptor) return null
            val icon = PlsIcons.FileTypes.ModeDescriptor
            return icon
        }
    }
}
