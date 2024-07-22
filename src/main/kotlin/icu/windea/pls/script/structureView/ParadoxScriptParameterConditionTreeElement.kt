package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxScriptParameterConditionTreeElement(element: ParadoxScriptParameterCondition) : PsiTreeElementBase<ParadoxScriptParameterCondition>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val parent = element
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = mutableListOf()
		parent.forEachChild {
			when {
				it is ParadoxScriptScriptedVariable -> result.add(ParadoxScriptVariableTreeElement(it))
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return "[" + element.conditionExpression + "]"
	}
}
