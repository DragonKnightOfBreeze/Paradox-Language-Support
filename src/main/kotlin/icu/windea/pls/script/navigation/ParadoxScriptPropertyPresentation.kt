package icu.windea.pls.script.navigation

import icu.windea.pls.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyPresentation(
	element: ParadoxScriptProperty
): ItemPresentationBase<ParadoxScriptProperty>(element){
	override fun getPresentableText(): String? {
		//如果是定义，则优先使用定义的名字
		val element = element ?: return null
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return definitionInfo.name
		return element.name
	}
}

