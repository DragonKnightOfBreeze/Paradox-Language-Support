package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter

class ParadoxLocalisationReferenceRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxLocalisationParameter) e.name else ""
        return PlsBundle.message("localisation.remove.reference", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationParameter
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
