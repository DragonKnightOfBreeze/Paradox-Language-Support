package icu.windea.pls.lang.refactoring.rename

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.ParadoxNameValidators
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement

class ParadoxLocalisationParameterRenameInputValidator : RenameInputValidator {
    private val elementPattern = PlatformPatterns.psiElement(ParadoxLocalisationParameterLightElement::class.java)

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return ParadoxNameValidators.checkLocalisationParameterName(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }
}
