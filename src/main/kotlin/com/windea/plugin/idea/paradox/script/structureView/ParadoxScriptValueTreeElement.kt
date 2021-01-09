package com.windea.plugin.idea.paradox.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptValueTreeElement(
	private val element: ParadoxScriptValue
): PsiTreeElementBase<ParadoxScriptValue>(element){
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		return when{
			element !is ParadoxScriptBlock -> mutableListOf()
			element.isArray -> element.valueList.mapTo(mutableListOf()){ParadoxScriptValueTreeElement(it)}
			element.isObject -> element.propertyList.mapTo(mutableListOf()){ParadoxScriptPropertyTreeElement(it)}
			else -> mutableListOf()
		}
	}

	override fun getPresentableText(): String? {
		return when{
			element is ParadoxScriptBlock -> blockFolder
			else -> element.text.truncate(20) //不去除包围的双引号
		}
	}
}

