package icu.windea.pls.lang.refactoring.rename

import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableRenameInputValidator : RenameInputValidator {
    private val elementPattern = psiElement(ParadoxScriptScriptedVariable::class.java)

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return PlsPatternConstants.scriptedVariableName.matches(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }
}

