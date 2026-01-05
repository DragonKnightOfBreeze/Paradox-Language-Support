package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 内联脚本的代码检查的基类。
 */
abstract class InlineScriptInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val gameType = selectGameType(file)
        if (!ParadoxInlineScriptManager.isSupported(gameType)) return false

        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)
    }
}
