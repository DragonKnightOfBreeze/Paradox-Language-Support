package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 内联脚本的代码检查的基类。
 */
abstract class InlineScriptInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求受游戏类型支持
        if (!ParadoxInlineScriptManager.isSupported(selectGameType(file))) return false
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的脚本文件（内联脚本文件中也能嵌套使用内联脚本，因此这里的约束是可行的）
        return ParadoxPsiFileMatcher.isScriptFile(file, ParadoxPathConstraint.AcceptInlineScriptUsage, injectable = true)
    }
}
