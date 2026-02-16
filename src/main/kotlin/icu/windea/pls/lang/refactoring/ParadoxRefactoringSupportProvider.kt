package icu.windea.pls.lang.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return when {
            element is ParadoxScriptScriptedVariable -> true
            element is ParadoxScriptProperty -> false // should not be available, since it can be a definition root key
            element is ParadoxScriptStringExpressionElement -> true
            element is ParadoxLocalisationProperty -> true
            // element is ParadoxParameterElement -> true // should be available, but cannot be
            // element is ParadoxDynamicValueElement -> true // should be available, but cannot be
            // element is ParadoxComplexEnumValueElement -> true // should be available, but cannot be
            else -> false
        }
    }
}
