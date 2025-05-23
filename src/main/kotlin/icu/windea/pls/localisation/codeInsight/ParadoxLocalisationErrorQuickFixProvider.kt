package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 快速修复一些语法错误。
 */
class ParadoxLocalisationErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language !is ParadoxLocalisationLanguage) return
        when {
            errorElement.prevSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
                //LEFT_QUOTE
                builder.registerFix(InsertMissingTokenFix("\"", errorElement.endOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
                //RIGHT_QUOTE
                builder.registerFix(InsertMissingTokenFix("\"", errorElement.startOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationParameter -> {
                //PARAMETER_END
                builder.registerFix(InsertMissingTokenFix("$", errorElement.startOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationIcon -> {
                //ICON_END
                builder.registerFix(InsertMissingTokenFix("£", errorElement.startOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationTextFormat -> {
                //TEXT_FORMAT_END
                builder.registerFix(InsertMissingTokenFix("#!", errorElement.startOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationTextIcon -> {
                //TEXT_ICON_END
                builder.registerFix(InsertMissingTokenFix("!", errorElement.startOffset), null, null, null, null)
            }
        }
    }
}
