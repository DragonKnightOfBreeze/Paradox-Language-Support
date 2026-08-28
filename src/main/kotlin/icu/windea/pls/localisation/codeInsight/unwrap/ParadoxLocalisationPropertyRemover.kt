package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class ParadoxLocalisationPropertyRemover : ParadoxLocalisationUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxLocalisationProperty
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxLocalisationProperty) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("localisation.remove.property", name.or.unresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxLocalisationProperty) return // unexpected
        context.delete(element)
    }
}
