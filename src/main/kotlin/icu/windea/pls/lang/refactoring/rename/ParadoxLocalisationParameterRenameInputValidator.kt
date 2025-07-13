package icu.windea.pls.lang.refactoring.rename

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.model.constants.PlsPatternConstants

class ParadoxLocalisationParameterRenameInputValidator : RenameInputValidator {
    private val elementPattern = PlatformPatterns.psiElement(ParadoxLocalisationParameterElement::class.java)

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return PlsPatternConstants.localisationParameterName.matches(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }
}
