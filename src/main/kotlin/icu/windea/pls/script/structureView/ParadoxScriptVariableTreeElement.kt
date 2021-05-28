package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableTreeElement(
	private val element: ParadoxScriptVariable?
): PsiTreeElementBase<ParadoxScriptVariable>(element){
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		return mutableListOf()
	}

	override fun getPresentableText(): String? {
		return element?.name
	}
}
