package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtPropertyTreeElement(
	private val element: CwtProperty
) : PsiTreeElementBase<CwtProperty>(element) {
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		val value = element.value ?: return mutableListOf()
		return when {
			value !is CwtBlock -> mutableListOf()
			value.isArray -> value.valueList.mapTo(mutableListOf()) { CwtValueTreeElement(it) }
			value.isObject -> value.propertyList.mapTo(mutableListOf()) { CwtPropertyTreeElement(it) }
			else -> mutableListOf()
		}
	}
	
	override fun getPresentableText(): String? {
		return element.name
	}
}

