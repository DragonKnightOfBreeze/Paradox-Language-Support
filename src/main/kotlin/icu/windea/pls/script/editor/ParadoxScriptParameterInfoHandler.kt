package icu.windea.pls.script.editor

import com.intellij.lang.parameterInfo.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.util.selector.*

//com.intellij.codeInsight.hint.api.impls.XmlParameterInfoHandler

/**
 * 显示`scripted_effect/scripted_trigger`等的参数信息。
 */
class ParadoxScriptParameterInfoHandler : ParameterInfoHandler<ParadoxScriptProperty, Set<String>> {
	private fun findProperty(context: ParameterInfoContext): ParadoxScriptProperty? {
		//向上找第一个scriptProperty，直到其作为子节点的scriptProperty可以匹配enum[scripted_effect_params]
		val element = context.file.findElementAt(context.offset) ?: return null
		return element.parents(true).filterIsInstance<ParadoxScriptProperty>().find {prop ->
			prop.definitionElementInfo?.takeIf { it.isValid }?.propertyConfigs?.any { propConfig -> 
				propConfig.properties?.any { 
					CwtConfigHandler.isInputParameter(it)
				} ?: false
			} ?: false
		}?.takeIf { result ->
			//光标位置必须位于block中
			result.propertyValue?.textRange?.let { r -> context.offset > r.startOffset && context.offset < r.endOffset } ?: false
		}
	}
	
	override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParadoxScriptProperty? {
		val result = findProperty(context) ?: return null
		val definitionName = result.name
		val selector = definitionSelector().gameTypeFrom(context.file).preferRootFrom(context.file)
		val definitions = findDefinitionsByType(definitionName, "scripted_effect|scripted_trigger", context.project, selector = selector)
		val parameterNamesSet = definitions.mapTo(mutableSetOf()) { it.parameterNames }
		if(parameterNamesSet.isEmpty() || parameterNamesSet.first().isNullOrEmpty()) return null
		context.itemsToShow = parameterNamesSet.toTypedArray()
		return result
	}
	
	override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptProperty? {
		val result = findProperty(context) ?: return null
		val current = context.parameterOwner
		if(current == null || current === result) return result
		return null
	}
	
	override fun updateUI(p: Set<String>, context: ParameterInfoUIContext) {
		//PARAM1, PARAM2, ...
		//不高亮特定的参数
		val text = p.joinToString()
		val startOffset = text.length
		val endOffset = text.length
		context.setupUIComponentPresentation(text, startOffset, endOffset, false, false, false, context.defaultParameterColor)
	}
	
	override fun updateParameterInfo(parameterOwner: ParadoxScriptProperty, context: UpdateParameterInfoContext) {
		context.parameterOwner = parameterOwner
	}
	
	override fun showParameterInfo(element: ParadoxScriptProperty, context: CreateParameterInfoContext) {
		context.showHint(element, element.textRange.startOffset + 1, this)
	}
}