package icu.windea.pls.cwt.editor.worldSelection

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtPsiService

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinWordSelectionFilter

class CwtWordSelectionFilter : Condition<PsiElement> {
    override fun value(e: PsiElement): Boolean {
        if (e.language !== CwtLanguage) return true
        if (CwtPsiService.findBlockFromSelfOrBraces(e) != null) return false
        return true
    }
}
