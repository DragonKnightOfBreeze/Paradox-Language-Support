package icu.windea.pls.lang.refactoring.rename.validators

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxNameValidators

class ParadoxParameterRenameInputValidator : ParadoxRenameInputValidator() {
    private val elementPattern = PlatformPatterns.psiElement(ParadoxParameterLightElement::class.java)

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return ParadoxNameValidators.checkParameterName(newName)
    }
}
