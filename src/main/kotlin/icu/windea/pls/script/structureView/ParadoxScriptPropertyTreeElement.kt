package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*

class ParadoxScriptPropertyTreeElement(element: ParadoxScriptProperty) : PsiTreeElementBase<ParadoxScriptProperty>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val parent = element.findValue<ParadoxScriptBlock>() ?: return emptyList()
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = SmartList()
		parent.forEachChild {
			when {
				it is ParadoxScriptVariable -> result.add(ParadoxScriptVariableTreeElement(it))
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
				it is ParadoxScriptParameterCondition -> result.add(ParadoxScriptParameterConditionTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		//如果是定义，则优先显示定义信息（名字+类型）
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return definitionInfo.name + ": " + definitionInfo.typesText
		return element.name
	}
	
	override fun getLocationString(): String? {
		val element = element ?: return null
		//如果存在，显示定义的本地化名字（最相关的本地化文本）
		val definitionInfo = element.definitionInfo ?: return null
		val primaryLocalisation = definitionInfo.resolvePrimaryLocalisation(element)
		if(primaryLocalisation != null) {
			//这里需要使用移除格式后的纯文本，这里返回的字符串不是HTML
			val localizedName = ParadoxLocalisationTextExtractor.extract(primaryLocalisation)
			return localizedName
		}
		return null
	}
}
