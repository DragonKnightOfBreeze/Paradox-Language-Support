package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyTreeElement(
	private val element: ParadoxScriptProperty
) : PsiTreeElementBase<ParadoxScriptProperty>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val value = element.propertyValue?.value ?: return emptyList()
		return when {
			value !is ParadoxScriptBlock -> emptyList()
			value.isArray -> value.valueList.map { ParadoxScriptValueTreeElement(it) }
			value.isObject -> value.propertyList.map { ParadoxScriptPropertyTreeElement(it) }
			else -> emptyList()
		}
	}
	
	override fun getPresentableText(): String {
		return element.name
	}
}
