package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.structureView.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueTreeElement(
	element: ParadoxScriptValue
) : PsiTreeElementBase<ParadoxScriptValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		if(element !is ParadoxScriptBlock) return emptyList()
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = SmartList()
		value.forEachChild {
			when{
				//忽略字符串需要被识别为标签的情况 - 这里没有必要
				//it is ParadoxScriptString && it.resolveTagConfig() != null -> return@forEachChild
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return when {
			element is ParadoxScriptBlock -> blockFolder
			else -> element.text //保留可能的包围的双引号
		}
	}
}

