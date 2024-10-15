package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 快速修复一些语法错误。
 */
class ParadoxLocalisationErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language != ParadoxLocalisationLanguage) return
        when {
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationIcon -> {
                //ICON_END
                builder.registerFix(InsertMissingTokenFix("£", errorElement.startOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationPropertyReference -> {
                //PROPERTY_REFERENCE_END
                builder.registerFix(InsertMissingTokenFix("$", errorElement.startOffset), null, null, null, null)
            }
            errorElement.prevSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
                //LEFT_QUOTE
                builder.registerFix(InsertMissingTokenFix("\"", errorElement.endOffset), null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
                //RIGHT_QUOTE
                builder.registerFix(InsertMissingTokenFix("\"", errorElement.startOffset), null, null, null, null)
            }
        }
    }
}
