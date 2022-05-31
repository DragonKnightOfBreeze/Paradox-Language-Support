package icu.windea.pls.localisation.refactoring

import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class ParadoxLocalisationPropertyRenameInputValidator : RenameInputValidator {
	companion object {
		private val elementPattern = psiElement(ParadoxLocalisationProperty::class.java)
	}
	
	override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
		return PlsPatterns.localisationPropertyNameRegex.matches(newName)
	}

	override fun getPattern(): ElementPattern<out PsiElement> {
		return elementPattern
	}
}

