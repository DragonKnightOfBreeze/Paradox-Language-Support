package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon

class ParadoxLocalisationTextIconRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxLocalisationTextIcon) e.name.orEmpty() else ""
        return PlsBundle.message("localisation.remove.textIcon", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationTextIcon
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
