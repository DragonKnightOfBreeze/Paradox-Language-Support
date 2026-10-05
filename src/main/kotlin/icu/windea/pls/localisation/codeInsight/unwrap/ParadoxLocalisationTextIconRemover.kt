package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon

class ParadoxLocalisationTextIconRemover : ParadoxLocalisationUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxLocalisationTextIcon
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxLocalisationTextIcon) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("localisation.remove.textIcon", name.orUnresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxLocalisationTextIcon) return // unexpected
        context.delete(element)
    }
}
