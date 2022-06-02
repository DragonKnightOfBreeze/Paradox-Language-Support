package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueTreeElement(
	element: ParadoxScriptValue
) : PsiTreeElementBase<ParadoxScriptValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val parent = element.castOrNull<ParadoxScriptBlock>() ?: return emptyList()
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = SmartList()
		parent.forEachChild {
			when{
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
				it is ParadoxScriptParameterCondition -> result.add(ParadoxScriptParameterConditionTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.value
	}
}

