package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand

class ParadoxLocalisationCommandRemover : ParadoxLocalisationUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxLocalisationCommand
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxLocalisationCommand) return "" // unexpected
        val text = element.presentableText
        return ChronicleBundle.message("localisation.remove.command", text)
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxLocalisationCommand) return // unexpected
        context.delete(element)
    }
}
