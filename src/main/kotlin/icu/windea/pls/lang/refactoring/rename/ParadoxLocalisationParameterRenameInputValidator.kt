package icu.windea.pls.lang.refactoring.rename

import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.model.constants.*

class ParadoxLocalisationParameterRenameInputValidator : RenameInputValidator {
    private val elementPattern = PlatformPatterns.psiElement(ParadoxLocalisationParameterElement::class.java)

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return PlsPatternConstants.localisationParameterName.matches(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }
}
