package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

/**
 * 与重载相关的代码检查的基类。
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
abstract class OverrideRelatedInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件
        val vFile = file.virtualFile ?: return false
        if (VirtualFileService.isLightFile(vFile)) return false
        val fileInfo = file.fileInfo ?: return false
        // 兼容平台测试
        if (ChronicleFacade.isUnitTestMode()) return true
        // 仅限游戏或模组文件
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return false
        // 仅限项目文件
        if (!ProjectFileIndex.getInstance(file.project).isInContent(vFile)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        return true
    }
}
