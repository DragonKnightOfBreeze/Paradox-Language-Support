package icu.windea.pls.lang.fixes.error

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.analysis.ErrorQuickFixProvider
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.fixes.InsertMissingTokenFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.constants.ChronicleStrings

/**
 * 快速修复一些语法错误。
 */
class ParadoxLocalisationErrorQuickFixProvider : ErrorQuickFixProvider {
    override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
        if (errorElement.language !== ParadoxLocalisationLanguage) return
        when {
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationParameter -> {
                // PARAMETER_END
                val fix = createFix(ChronicleStrings.parameterEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationCommand -> {
                // RIGHT_BRACKET
                val fix = createFix(ChronicleStrings.commandEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationConceptCommand -> {
                // RIGHT_BRACKET
                val fix = createFix(ChronicleStrings.commandEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationIcon -> {
                // ICON_END
                val fix = createFix(ChronicleStrings.iconEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationTextIcon -> {
                // TEXT_ICON_END
                val fix = createFix(ChronicleStrings.textIconEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
            errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationTextFormat -> {
                // TEXT_FORMAT_END
                val fix = createFix(ChronicleStrings.textFormatEndMarker, errorElement.startOffset)
                builder.registerFix(fix, null, null, null, null)
            }
        }
    }

    private fun createFix(token: String, offset: Int): InsertMissingTokenFix {
        return InsertMissingTokenFix(ChronicleInspectionBundle.message("fix.insertMissingToken", token), token, offset)
    }
}
