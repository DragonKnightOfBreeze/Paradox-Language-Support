package icu.windea.pls.config.configGroup

import com.intellij.ide.*
import com.intellij.ide.projectView.impl.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.configGroup.*
import javax.swing.*

/**
 * 为规则分组所在的目录以及CWT规则文件提供特定的图标。
 */
class CwtConfigIconProvider : IconProvider(), DumbAware {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        when {
            element is PsiDirectory -> {
                val file = element.virtualFile
                val project = element.project
                if (ProjectRootsUtil.isModuleContentRoot(file, project)) return null
                if (ProjectRootsUtil.isModuleSourceRoot(file, project)) return null
                val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
                val fileProvider = fileProviders.find { it.getRootDirectory(project) == file }
                if (fileProvider == null) return null
                return PlsIcons.ConfigGroupDirectory
            }
            element is CwtFile -> {
                val file = element.virtualFile
                val project = element.project
                val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
                val fileProvider = fileProviders.find { it.getContainingConfigGroup(file, project) != null }
                if (fileProvider == null) return null
                return PlsIcons.FileTypes.CwtConfig
            }
            else -> return null
        }
    }
}
