package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxLocalisationStellarisFormatPsiReference(
	element: ParadoxLocalisationStellarisFormatReference,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationStellarisFormatReference>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return ResolveResult.EMPTY_ARRAY
	}
}