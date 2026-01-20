package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 内联脚本的代码检查的基类。
 */
abstract class InlineScriptInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求受游戏类型支持
        if (!ParadoxInlineScriptManager.isSupported(selectGameType(file))) return false
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)
    }
}
