package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.util.*
import icu.windea.pls.cwt.psi.*

class CwtFileTreeElement(
	element: CwtFile
) : PsiTreeElementBase<CwtFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val rootBlock = element.block ?: return emptyList()
		return PsiTreeUtil.getChildrenOfAnyType(
			rootBlock,
			CwtProperty::class.java,
			CwtValue::class.java
		).mapTo(mutableListOf()) {
			when(it) {
				is CwtProperty -> CwtPropertyTreeElement(it)
				is CwtValue -> CwtValueTreeElement(it)
				else -> throw InternalError()
			}
		}
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.name
	}
}