package icu.windea.pls.script.refactoring

import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*

class ParadoxScriptPropertyRenameInputValidator : RenameInputValidator {
	companion object {
		private val regex = "[^#@=\\s{}]+[^=\\s{}]*".toRegex()
		private val elementPattern = or(psiElement(PROPERTY))
	}
	
	override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
		return regex.matches(newName)
	}

	override fun getPattern(): ElementPattern<out PsiElement> {
		return elementPattern
	}
}

