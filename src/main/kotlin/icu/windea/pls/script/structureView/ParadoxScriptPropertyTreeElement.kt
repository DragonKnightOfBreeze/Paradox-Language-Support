package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyTreeElement(element: ParadoxScriptProperty) : PsiTreeElementBase<ParadoxScriptProperty>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val value = element.propertyValue?.value ?: return emptyList()
		if(value !is ParadoxScriptBlock) return emptyList()
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = SmartList()
		value.forEachChild {
			when{
				//忽略字符串需要被识别为标签的情况
				it is ParadoxScriptString && it.resolveTagConfig() != null -> return@forEachChild
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.name
	}
	
	override fun getLocationString(): String? {
		val element = element ?: return null
		val definitionInfo = element.definitionInfo ?: return null
		val name = definitionInfo.name
		val typesText = definitionInfo.typesText
		//如果definitionName和rootKey相同，则省略definitionName
		if(name.equals(definitionInfo.rootKey, true)) {
			return ": $typesText"
		} else {
			return "$name: $typesText"
		}
	}
}
