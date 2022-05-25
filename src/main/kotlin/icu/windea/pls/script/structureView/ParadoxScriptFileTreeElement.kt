package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFileTreeElement(
	element: ParadoxScriptFile
) : PsiTreeElementBase<ParadoxScriptFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val rootBlock = element.block ?: return emptyList()
		val result = SmartList<StructureViewTreeElement>()
		rootBlock.forEachChild {
			when {
				it is ParadoxScriptVariable -> result.add(ParadoxScriptVariableTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.name
	}
	
	override fun getLocationString(): String? {
		//如果文件名是descriptor.mod（不区分大小写），这里不要显示定义信息
		val element = element ?: return null
		if(element.name.equals(descriptorFileName, true)) return null
		val definitionInfo = element.definitionInfo ?: return null
		//如果definitionName和rootKey相同，则省略definitionName
		val name = definitionInfo.name
		val typesText = definitionInfo.typesText
		if(name.equals(definitionInfo.rootKey, true)){
			return ": $typesText"
		} else {
			return "$name: $typesText"
		}
	}
}
