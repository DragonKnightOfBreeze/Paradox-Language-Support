package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*

class CwtValueTreeElement(
	element: CwtValue
) : PsiTreeElementBase<CwtValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		if(element !is CwtBlock) return emptyList()
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = mutableListOf()
		element.forEachChild {
			when {
				it is CwtValue -> result.add(CwtValueTreeElement(it))
				it is CwtProperty -> result.add(CwtPropertyTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.value
	}
}
