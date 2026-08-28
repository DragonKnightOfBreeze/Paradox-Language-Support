package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat

class ParadoxLocalisationTextFormatUnwrapper : ParadoxLocalisationUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxLocalisationTextFormat
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxLocalisationTextFormat) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("localisation.unwrap.textFormat", name.or.unresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxLocalisationTextFormat) return // unexpected
        val textFormatText = element.textFormatText
        if (textFormatText != null) context.extract(element, textFormatText)
        context.delete(element)
    }
}
