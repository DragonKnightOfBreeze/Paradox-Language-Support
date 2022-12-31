package icu.windea.pls.localisation.references

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationColorCompletionProvider
 */
class ParadoxLocalisationColorPsiReference(
	element: ParadoxLocalisationColorfulText,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationColorfulText>(element, rangeInElement), SmartPsiReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		return element.colorConfig?.pointer?.element
	}
	
	override fun resolveTextAttributesKey(): TextAttributesKey? {
		return element.colorConfig?.color?.let { ParadoxLocalisationAttributesKeys.getColorKey(it) }
	}
}
