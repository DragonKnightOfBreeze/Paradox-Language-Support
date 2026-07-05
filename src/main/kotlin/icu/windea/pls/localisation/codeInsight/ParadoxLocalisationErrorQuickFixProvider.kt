package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.analysis.ErrorQuickFixProvider
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.lang.fixes.InsertMissingTokenFix
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.constants.ChronicleStrings

/**
 * 快速修复一些语法错误。
 */
class ParadoxLocalisationErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language !is ParadoxLocalisationLanguage) return
        when {
            errorElement.prevSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
                // LEFT_QUOTE
                val fix = InsertMissingTokenFix("\"", errorElement.endOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
                // RIGHT_QUOTE
                val fix = InsertMissingTokenFix("\"", errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationParameter -> {
                // PARAMETER_END
                val fix = InsertMissingTokenFix(ChronicleStrings.parameterEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationIcon -> {
                // ICON_END
                val fix = InsertMissingTokenFix(ChronicleStrings.iconEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationTextIcon -> {
                // TEXT_ICON_END
                val fix = InsertMissingTokenFix(ChronicleStrings.textIconEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationTextFormat -> {
                // TEXT_FORMAT_END
                val fix = InsertMissingTokenFix(ChronicleStrings.textFormatEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
        }
    }
}
