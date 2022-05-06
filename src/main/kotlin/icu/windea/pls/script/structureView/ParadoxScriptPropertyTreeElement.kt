package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyTreeElement(element: ParadoxScriptProperty) : PsiTreeElementBase<ParadoxScriptProperty>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val value = element.propertyValue?.value ?: return emptyList()
		return when {
			value !is ParadoxScriptBlock -> emptyList()
			value.isArray -> value.valueList.map { ParadoxScriptValueTreeElement(it) }
			value.isObject -> value.propertyList.map { ParadoxScriptPropertyTreeElement(it) }
			else -> emptyList()
		}
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.name
	}
	
	override fun getLocationString(): String? {
		val element = element ?: return null
		val definitionInfo = element.definitionInfo ?: return null
		//如果definitionName和rootKey相同，则省略definitionName
		val name = definitionInfo.name
		val typesText = definitionInfo.typesText
		if(name.equals(definitionInfo.rootKey, true)) {
			return ": $typesText"
		} else {
			return "$name: $typesText"
		}
	}
}
