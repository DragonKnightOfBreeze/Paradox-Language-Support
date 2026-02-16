package icu.windea.pls.lang.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // NOTE 2.1.3 测试时需要禁用 inplaceRename，以便 `myFixture.renameElementAtCaretUsingHandler(newName)` 正常生效
        if (PlsFacade.isUnitTestMode()) return false

        return when {
            element is ParadoxScriptScriptedVariable -> true
            element is ParadoxScriptProperty -> isMemberInplaceRenameAvailable(element, context) // may be available
            element is ParadoxScriptStringExpressionElement -> true
            element is ParadoxLocalisationProperty -> true
            // element is ParadoxParameterElement -> true // should be available, but cannot be
            // element is ParadoxDynamicValueElement -> true // should be available, but cannot be
            // element is ParadoxComplexEnumValueElement -> true // should be available, but cannot be
            else -> false
        }
    }

    private fun isMemberInplaceRenameAvailable(element: ParadoxScriptProperty, context: PsiElement?): Boolean {
        element.definitionInfo?.let { return ParadoxPsiManager.isInplaceRenameAvailableForDefinition(element, context, it) }
        return false
    }
}
