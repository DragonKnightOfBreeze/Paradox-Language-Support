package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText

class ParadoxLocalisationColorfulTextRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxLocalisationColorfulText) e.name.orEmpty() else ""
        return PlsBundle.message("localisation.remove.color", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationColorfulText
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
