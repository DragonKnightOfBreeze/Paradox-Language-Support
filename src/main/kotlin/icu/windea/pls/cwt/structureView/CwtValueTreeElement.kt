package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

class CwtValueTreeElement(
	private val element: CwtValue
) : PsiTreeElementBase<CwtValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		return when {
			element !is CwtBlock -> emptyList()
			element.isArray -> element.valueList.map { CwtValueTreeElement(it) }
			element.isObject -> element.propertyList.map { CwtPropertyTreeElement(it) }
			else -> emptyList()
		}
	}
	
	override fun getPresentableText(): String? {
		return when {
			element is CwtBlock -> blockFolder
			element is CwtString -> element.truncatedValue
			else -> element.text
		}
	}
}