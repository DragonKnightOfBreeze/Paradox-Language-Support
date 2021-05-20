package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyTreeElement(
	private val element: ParadoxScriptProperty
) : PsiTreeElementBase<ParadoxScriptProperty>(element) {
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		val value = element.propertyValue?.value?:return mutableListOf()
		return when{
			value !is ParadoxScriptBlock -> mutableListOf()
			value.isArray -> value.valueList.mapTo(mutableListOf()){ParadoxScriptValueTreeElement(it)}
			value.isObject -> value.propertyList.mapTo(mutableListOf()){ParadoxScriptPropertyTreeElement(it)}
			else -> mutableListOf()
		}
	}

	override fun getPresentableText(): String? {
		return element.name
	}
}
