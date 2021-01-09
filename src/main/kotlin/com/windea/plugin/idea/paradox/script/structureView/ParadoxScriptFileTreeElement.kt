package com.windea.plugin.idea.paradox.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptFileTreeElement(
	private val element: ParadoxScriptFile
) : PsiTreeElementBase<ParadoxScriptFile>(element) {
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		val rootBlock = element.rootBlock ?: return mutableListOf()
		return PsiTreeUtil.getChildrenOfAnyType(
			rootBlock,
			ParadoxScriptVariable::class.java,
			ParadoxScriptProperty::class.java,
			ParadoxScriptValue::class.java
		).mapTo(mutableListOf()) {
			when(it) {
				is ParadoxScriptVariable -> ParadoxScriptVariableTreeElement(it)
				is ParadoxScriptProperty -> ParadoxScriptPropertyTreeElement(it)
				is ParadoxScriptValue -> ParadoxScriptValueTreeElement(it)
				else -> throw InternalError()
			}
		}
	}
	
	override fun getPresentableText(): String? {
		return element.name
	}
}
