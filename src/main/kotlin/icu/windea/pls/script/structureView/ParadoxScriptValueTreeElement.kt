package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueTreeElement(
	private val element: ParadoxScriptValue
) : PsiTreeElementBase<ParadoxScriptValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		return when {
			element !is ParadoxScriptBlock -> emptyList()
			element.isArray -> element.valueList.map { ParadoxScriptValueTreeElement(it) }
			element.isObject -> element.propertyList.map { ParadoxScriptPropertyTreeElement(it) }
			else -> emptyList()
		}
	}
	
	override fun getPresentableText(): String {
		return when {
			element is ParadoxScriptBlock -> blockFolder
			else -> element.text.truncate(truncateLimit) //不去除包围的双引号
		}
	}
}

