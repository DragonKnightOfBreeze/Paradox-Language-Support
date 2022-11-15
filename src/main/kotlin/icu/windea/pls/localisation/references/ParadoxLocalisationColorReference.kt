package icu.windea.pls.localisation.references

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationColorCompletionProvider
 */
class ParadoxLocalisationColorReference(
	element: ParadoxLocalisationColorfulText,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationColorfulText>(element, rangeInElement), PsiAnnotatedReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun resolve(): PsiElement? {
		return element.colorConfig?.pointer?.element
	}
	
	override fun resolveTextAttributesKey(): TextAttributesKey? {
		return element.colorConfig?.color?.let { ParadoxLocalisationAttributesKeys.getColorKey(it) }
	}
}
