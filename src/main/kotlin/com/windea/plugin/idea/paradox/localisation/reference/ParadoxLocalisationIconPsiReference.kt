package com.windea.plugin.idea.paradox.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationIconPsiReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationIcon>(element,rangeInElement){
	override fun resolve(): PsiElement? {
		//TODO
		return null
	}
}