package com.windea.plugin.idea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.cwt.psi.*

class CwtValueTreeElement(
	private val element: CwtValue
): PsiTreeElementBase<CwtValue>(element){
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		return when{
			element !is CwtBlock -> mutableListOf()
			element.isArray -> element.valueList.mapTo(mutableListOf()){ CwtValueTreeElement(it) }
			element.isObject -> element.propertyList.mapTo(mutableListOf()){ CwtPropertyTreeElement(it) }
			else -> mutableListOf()
		}
	}
	
	override fun getPresentableText(): String? {
		return when{
			element is CwtBlock -> blockFolder
			element is CwtString -> element.truncatedValue
			else -> element.text
		}
	}
}