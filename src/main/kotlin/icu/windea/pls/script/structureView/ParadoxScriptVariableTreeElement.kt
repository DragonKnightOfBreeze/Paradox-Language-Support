package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableTreeElement(
	element: ParadoxScriptVariable
) : PsiTreeElementBase<ParadoxScriptVariable>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		return emptyList()
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		val name = element.name
		val value = element.value
		return if(value == null) name else "$name=$value"
	}
}
