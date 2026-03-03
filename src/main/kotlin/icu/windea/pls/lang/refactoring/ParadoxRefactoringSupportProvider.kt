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
            element is ParadoxScriptProperty -> isMemberInplaceRenameAvailable(element, context)
            element is ParadoxScriptStringExpressionElement -> true
            element is ParadoxLocalisationProperty -> true
            // element is ParadoxComplexEnumValueLightElement -> true // not available since textRange and textOffset are not provided yet
            // element is ParadoxDynamicValueLightElement -> true // not available since textRange and textOffset are not provided yet
            // element is ParadoxParameterLightElement -> true // not available since textRange and textOffset are not provided yet
            // element is ParadoxLocalisationParameterLightElement -> true // not available since textRange and textOffset are not provided yet
            // element is ParadoxModifierLightElement -> true // not available since textRange and textOffset are not provided yet
            else -> false
        }
    }

    private fun isMemberInplaceRenameAvailable(element: ParadoxScriptProperty, context: PsiElement?): Boolean {
        element.definitionInfo?.let { return ParadoxPsiManager.isInplaceRenameAvailableForDefinition(element, context, it) }
        return false
    }
}
