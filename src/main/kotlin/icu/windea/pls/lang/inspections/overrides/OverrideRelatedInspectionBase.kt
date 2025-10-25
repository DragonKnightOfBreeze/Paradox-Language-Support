package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 与重载相关的代码检查的基类。
 */
abstract class OverrideRelatedInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        if (PlsFileManager.isLightFile(vFile)) return false // skip for in-memory files
        val fileInfo = file.fileInfo
        if (fileInfo == null) return false // only for game or mod files
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return false // only for game or mod files
        if (!ProjectFileIndex.getInstance(file.project).isInContent(vFile)) return false // only for project files
        return true
    }
}
