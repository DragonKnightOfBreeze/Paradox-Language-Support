package icu.windea.pls.core.refactoring.rename

import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*

class ParadoxParameterRenameInputValidator : RenameInputValidator {
	private val elementPattern = PlatformPatterns.psiElement(ParadoxParameter::class.java)
	
	override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
		return PlsConstants.Patterns.parameterNameRegex.matches(newName)
	}
	
	override fun getPattern(): ElementPattern<out PsiElement> {
		return elementPattern
	}
}
