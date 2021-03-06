package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFileTreeElement(
	private val element: ParadoxScriptFile
) : PsiTreeElementBase<ParadoxScriptFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val rootBlock = element.block ?: return mutableListOf()
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
	
	override fun getPresentableText(): String {
		return element.name
	}
}
