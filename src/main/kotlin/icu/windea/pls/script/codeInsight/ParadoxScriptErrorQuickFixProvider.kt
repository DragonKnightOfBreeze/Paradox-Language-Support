package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.analysis.ErrorQuickFixProvider
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.lang.quickfix.InsertMissingTokenFix
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxParameter

/**
 * 快速修复一些语法错误。
 */
class ParadoxScriptErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language !is ParadoxScriptLanguage) return
        when {
            errorElement.nextSibling == null && errorElement.parent is ParadoxParameter -> {
                //PARAMETER_END
                builder.registerFix(InsertMissingTokenFix("$", errorElement.startOffset), null, null, null, null)
            }
        }
    }
}
