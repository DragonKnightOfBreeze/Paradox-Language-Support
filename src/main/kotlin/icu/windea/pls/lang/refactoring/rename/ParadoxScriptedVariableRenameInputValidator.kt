package icu.windea.pls.lang.refactoring.rename

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.PlsNameValidators
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptedVariableRenameInputValidator : RenameInputValidator {
    private val elementPattern = psiElement(ParadoxScriptScriptedVariable::class.java)

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return PlsNameValidators.checkScriptedVariableName(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return elementPattern
    }
}

