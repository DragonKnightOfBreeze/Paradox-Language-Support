package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

class ParadoxLocalisationIconRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxLocalisationIcon) e.name.orEmpty() else ""
        return PlsBundle.message("localisation.remove.icon", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationIcon
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
