package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.cwt.psi.*

class CwtPropertyTreeElement(
	private val element: CwtProperty
) : PsiTreeElementBase<CwtProperty>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val value = element.value ?: return emptyList()
		return when {
			value !is CwtBlock -> emptyList()
			value.isArray -> value.valueList.map { CwtValueTreeElement(it) }
			value.isObject -> value.propertyList.map { CwtPropertyTreeElement(it) }
			else -> emptyList()
		}
	}
	
	override fun getPresentableText(): String? {
		return element.name
	}
}

