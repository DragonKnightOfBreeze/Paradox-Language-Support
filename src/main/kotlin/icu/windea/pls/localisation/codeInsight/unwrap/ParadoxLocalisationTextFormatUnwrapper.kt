package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat

class ParadoxLocalisationTextFormatUnwrapper : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxLocalisationTextFormat) e.name.orEmpty() else ""
        return PlsBundle.message("localisation.unwrap.textFormat", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationTextFormat
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element is ParadoxLocalisationTextFormat) {
            val textFormatText = element.textFormatText
            if (textFormatText != null) {
                context.extract(element, textFormatText)
            }
        }
        context.delete(element)
    }
}
