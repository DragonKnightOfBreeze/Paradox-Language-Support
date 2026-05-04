package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 与重载相关的代码检查的基类。
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
abstract class OverrideRelatedInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        if (VirtualFileService.isLightFile(vFile)) return false // skip for in-memory files
        val fileInfo = file.fileInfo
        if (fileInfo == null) return false // only for game or mod files
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return false // only for game or mod files
        if (!ProjectFileIndex.getInstance(file.project).isInContent(vFile)) return false // only for project files
        return true
    }
}
