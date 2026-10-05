package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter

class ParadoxLocalisationParameterRemover : ParadoxLocalisationUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxLocalisationParameter
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxLocalisationParameter) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("localisation.remove.parameter", name.orUnresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxLocalisationParameter) return // unexpected
        context.delete(element)
    }
}
