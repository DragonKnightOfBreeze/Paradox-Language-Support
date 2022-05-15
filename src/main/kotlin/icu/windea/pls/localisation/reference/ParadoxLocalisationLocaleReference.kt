package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

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
	
	@Suppress("RedundantOverride")
	override fun getVariants(): Array<Any> {
		return super.getVariants() //交由ParadoxLocaleCompletionProvider实现
	}
}