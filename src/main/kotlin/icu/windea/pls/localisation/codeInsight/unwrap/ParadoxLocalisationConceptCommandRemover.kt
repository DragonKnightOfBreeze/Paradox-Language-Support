package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand

class ParadoxLocalisationConceptCommandRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val text = if (e is ParadoxLocalisationConceptCommand) e.text else ""
        return PlsBundle.message("localisation.remove.conceptCommand", text)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationConceptCommand
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
