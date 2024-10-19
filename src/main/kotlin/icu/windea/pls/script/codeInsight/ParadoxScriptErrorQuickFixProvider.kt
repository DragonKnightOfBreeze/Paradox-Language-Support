package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.script.*

/**
 * 快速修复一些语法错误。
 */
class ParadoxScriptErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language != ParadoxScriptLanguage) return
        when {
            errorElement.nextSibling == null && errorElement.parent is ParadoxParameter -> {
                //PARAMETER_END
                builder.registerFix(InsertMissingTokenFix("$", errorElement.startOffset), null, null, null, null)
            }
        }
    }
}
