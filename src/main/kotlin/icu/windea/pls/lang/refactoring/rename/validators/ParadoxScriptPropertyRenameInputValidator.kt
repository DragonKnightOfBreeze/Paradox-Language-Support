package icu.windea.pls.lang.refactoring.rename.validators

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertyRenameInputValidator : ParadoxRenameInputValidator() {
    private val elementPattern = psiElement(ParadoxScriptProperty::class.java)

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return ParadoxNameValidators.checkScriptPropertyName(newName)
    }
}
