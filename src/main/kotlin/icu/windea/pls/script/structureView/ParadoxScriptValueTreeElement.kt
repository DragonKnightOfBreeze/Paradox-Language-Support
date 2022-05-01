package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueTreeElement(
	element: ParadoxScriptValue
) : PsiTreeElementBase<ParadoxScriptValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		return when {
			element !is ParadoxScriptBlock -> emptyList()
			element.isArray -> element.valueList.map { ParadoxScriptValueTreeElement(it) }
			element.isObject -> element.propertyList.map { ParadoxScriptPropertyTreeElement(it) }
			else -> emptyList()
		}
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return when {
			element is ParadoxScriptBlock -> blockFolder
			else -> element.text.let { it.truncateAndKeepQuotes(truncateLimit) } //保留可能的包围的双引号
		}
	}
}

