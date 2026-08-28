package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand

class ParadoxLocalisationConceptCommandRemover : ParadoxLocalisationUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxLocalisationConceptCommand
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxLocalisationConceptCommand) return "" // unexpected
        val text = element.presentableText
        return ChronicleBundle.message("localisation.remove.conceptCommand", text)
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxLocalisationConceptCommand) return // unexpected
        context.delete(element)
    }
}
