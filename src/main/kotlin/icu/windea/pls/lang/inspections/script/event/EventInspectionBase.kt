package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 事件的代码检查的基类。
 */
abstract class EventInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val vFile = file.virtualFile
        if (VirtualFileService.isLightFile(vFile)) return false
        if (VirtualFileService.isInjectedFile(vFile)) return false
        // 要求受游戏类型支持
        val gameType = selectGameType(file)
        if (gameType == null || gameType == ParadoxGameType.Core) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件（仅限的脚本文件）
        return ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.ForEvent)
    }
}
