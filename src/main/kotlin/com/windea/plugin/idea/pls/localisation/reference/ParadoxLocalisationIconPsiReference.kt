package com.windea.plugin.idea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ParadoxLocalisationIconPsiReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationIcon>(element,rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	override fun resolve(): PsiElement? {
		//TODO
		return null
	}
}