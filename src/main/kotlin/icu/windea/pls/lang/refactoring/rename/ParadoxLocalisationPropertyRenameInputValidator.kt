package icu.windea.pls.lang.refactoring.rename

import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*

class ParadoxLocalisationPropertyRenameInputValidator : RenameInputValidator {
    private val elementPattern = psiElement(ParadoxLocalisationProperty::class.java)

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return PlsPatternConstants.localisationPropertyName.matches(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }
}

