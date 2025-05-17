package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

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
