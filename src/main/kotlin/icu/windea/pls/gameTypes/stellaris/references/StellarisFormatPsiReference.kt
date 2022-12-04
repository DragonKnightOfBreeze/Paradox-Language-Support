package icu.windea.pls.gameTypes.stellaris.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.gameTypes.stellaris.psi.*
import icu.windea.pls.localisation.gameTypes.stellaris.psi.*

class StellarisFormatPsiReference(
	element: StellarisFormatReference,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<StellarisFormatReference>(element, rangeInElement) {
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