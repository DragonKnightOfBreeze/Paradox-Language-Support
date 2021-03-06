package icu.windea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationCommandScopeReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationCommandScope>(element,rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		//TODO
		return null
	}
	
	override fun getVariants(): Array<out Any> {
		//TODO
		return emptyArray()
	}
}