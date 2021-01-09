package com.windea.plugin.idea.paradox.script.refactoring

import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

class ParadoxScriptVariableRenameInputValidator : RenameInputValidator {
	private val regex = "@[a-zA-Z0-9_-]+".toRegex()
	private val pattern = or(psiElement(VARIABLE),psiElement(VARIABLE_REFERENCE))
	
	override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
		return regex.matches(newName)
	}

	override fun getPattern(): ElementPattern<out PsiElement> {
		return pattern
	}
}

