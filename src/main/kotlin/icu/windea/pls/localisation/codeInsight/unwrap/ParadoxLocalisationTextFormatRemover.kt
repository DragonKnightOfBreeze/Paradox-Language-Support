package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat

class ParadoxLocalisationTextFormatRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxLocalisationTextFormat) e.name.orEmpty() else ""
        return PlsBundle.message("localisation.remove.textFormat", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationTextFormat
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
