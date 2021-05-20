package icu.windea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationCommandScopePsiReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationCommandScope>(element,rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	override fun resolve(): PsiElement? {
		//TODO
		return null
	}
}