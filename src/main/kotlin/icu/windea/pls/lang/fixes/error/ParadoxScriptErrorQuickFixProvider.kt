package icu.windea.pls.lang.fixes.error

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.analysis.ErrorQuickFixProvider
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.fixes.InsertMissingTokenFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMath

/**
 * 快速修复一些语法错误。
 */
class ParadoxScriptErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language !== ParadoxScriptLanguage) return
        when {
            errorElement.nextSibling == null && errorElement.parent is ParadoxParameter -> {
                // PARAMETER_END
                val fix = createFix(ChronicleStrings.parameterEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxScriptInlineMath -> {
                // INLINE_MATH_END
                val fix = createFix(ChronicleStrings.inlineMathEnd, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
        }
    }

    private fun createFix(token: String, offset: Int): InsertMissingTokenFix {
        return InsertMissingTokenFix(ChronicleInspectionBundle.message("fix.insertMissingToken", token), token, offset)
    }
}
