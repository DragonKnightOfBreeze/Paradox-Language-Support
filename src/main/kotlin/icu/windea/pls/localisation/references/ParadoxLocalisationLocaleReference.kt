package icu.windea.pls.localisation.references

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationLocaleCompletionProvider
 */
class ParadoxLocalisationLocaleReference(
	element: ParadoxLocalisationLocale,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationLocale>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun resolve(): PsiElement? {
		return element.localeConfig?.pointer?.element
	}
}
