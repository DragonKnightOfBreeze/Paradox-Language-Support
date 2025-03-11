package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.ide.projectView.impl.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 为游戏或模组目录，以及模组描述符文件，提供特定的图标。
 */
class ParadoxIconProvider : IconProvider(), DumbAware {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        when {
            element is PsiDirectory -> {
                val file = element.virtualFile
                val project = element.project
                val rootInfo = file.fileInfo?.rootInfo ?: return null
                if (file != rootInfo.rootFile) return null
                if (ProjectRootsUtil.isModuleContentRoot(file, project)) return null
                if (ProjectRootsUtil.isModuleSourceRoot(file, project)) return null
                val icon = when (rootInfo) {
                    is ParadoxRootInfo.Game -> PlsIcons.GameDirectory
                    is ParadoxRootInfo.Mod -> PlsIcons.ModDirectory
                }
                return icon
            }
            element is ParadoxScriptFile -> {
                val file = element.virtualFile ?: return null
                if (file.fileInfo == null) return null
                if (file.name.endsWith(".mod", true)) return PlsIcons.FileTypes.ModeDescriptor
                return null
            }
            else -> return null
        }
    }
}
