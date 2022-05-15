package icu.windea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationColorReference(
	element: ParadoxLocalisationColorfulText,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationColorfulText>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException(PlsBundle.message("cannotBeRenamed")) //不允许重命名
	}
	
	override fun resolve(): PsiElement? {
		return element.colorConfig?.pointer?.element
	}
	
	@Suppress("RedundantOverride")
	override fun getVariants(): Array<Any> {
		return super.getVariants() //不需要进行提示
	}
}