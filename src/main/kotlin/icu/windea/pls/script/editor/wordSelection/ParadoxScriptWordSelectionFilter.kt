package icu.windea.pls.script.editor.wordSelection

import com.intellij.psi.PsiElement
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptPsiService

class ParadoxScriptWordSelectionFilter : com.intellij.openapi.util.Condition<PsiElement> {
    override fun value(e: PsiElement): Boolean {
        if (e.language !== ParadoxScriptLanguage) return true
        if (ParadoxScriptPsiService.findBlockFromSelfOrBraces(e) != null) return false
        if (ParadoxScriptPsiService.findConditionalBlockFromSelfOrBrackets(e) != null) return false
        if (ParadoxScriptPsiService.findInlineMathFromSelfOrBrackets(e) != null) return false
        return true
    }
}
