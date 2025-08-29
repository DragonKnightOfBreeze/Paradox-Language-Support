package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand

class ParadoxLocalisationCommandRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val text = if (e is ParadoxLocalisationCommand) e.text else ""
        return PlsBundle.message("localisation.remove.command", text)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationCommand
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
