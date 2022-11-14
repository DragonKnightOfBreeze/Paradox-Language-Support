package icu.windea.pls.script.editor

import com.intellij.lang.parameterInfo.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

/**
 * 显示SV表达式的参数信息（如果有）。
 */
class ParadoxScriptValueExpressionParameterInfoHandler : ParameterInfoHandler<ParadoxScriptExpressionElement, Set<String>> {
	//2. 光标位置位于带参数的SV表达式的参数部分中，即在value:xxx|xxx|xxx|的第一个管道符之后
	
	override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParadoxScriptExpressionElement? {
		val offset = context.offset
		val element = context.file.findElementAt(offset) ?: return null
		val targetElement = element.parent.castOrNull<ParadoxScriptExpressionElement>() ?: return null
		
		val text = targetElement.text
		if(!text.contains("value:") || !text.contains('|')) return null //快速判断
		val config = ParadoxCwtConfigHandler.resolveConfig(targetElement) ?: return null
		val dataType = config.expression.type
		if(dataType != CwtDataTypes.ValueField && dataType != CwtDataTypes.IntValueField) return null
		val configGroup = config.info.configGroup
		val valueFieldExpression = ParadoxScriptExpression.resolveValueField(text, configGroup)
		val prefixInfo = valueFieldExpression.prefixInfo ?: return null
		if(prefixInfo.text != "value:") return null
		val dataSourceInfo = valueFieldExpression.dataSourceInfo ?: return null
		//要求光标位置在SV表达式中的参数部分中
		if(offset - targetElement.textRange.startOffset <= valueFieldExpression.scriptValueParametersStartIndex) return null
		
		//合并所有可能的参数名
		val svName = dataSourceInfo.text
		val selector = definitionSelector().gameTypeFrom(context.file).preferRootFrom(context.file)
		val definitions = ParadoxDefinitionSearch.search(svName, "script_value", context.project, selector = selector).findAll()
		val parameterNamesSet = definitions.mapNotNullTo(mutableSetOf()) { definition ->
			definition.parameterMap.keys.ifEmpty { setOf(PlsDocBundle.message("noParameters")) }
		}
		if(parameterNamesSet.isEmpty()) return null
		context.itemsToShow = parameterNamesSet.toTypedArray()
		return targetElement
	}
	
	override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptExpressionElement? {
		val offset = context.offset
		val element = context.file.findElementAt(offset) ?: return null
		val targetElement = element.parent.castOrNull<ParadoxScriptExpressionElement>() ?: return null
		val current = context.parameterOwner
		if(current != null && current !== targetElement) return null
		
		val text = targetElement.text
		if(!text.contains("value:") || !text.contains('|')) return null //快速判断
		val config = ParadoxCwtConfigHandler.resolveConfig(targetElement) ?: return null
		val dataType = config.expression.type
		if(dataType != CwtDataTypes.ValueField && dataType != CwtDataTypes.IntValueField) return null
		val configGroup = config.info.configGroup
		val valueFieldExpression = ParadoxScriptExpression.resolveValueField(text, configGroup)
		val prefixInfo = valueFieldExpression.prefixInfo ?: return null
		if(prefixInfo.text != "value:") return null
		valueFieldExpression.dataSourceInfo ?: return null
		//要求光标位置在SV表达式中的参数部分中
		if(offset - targetElement.textRange.startOffset <= valueFieldExpression.scriptValueParametersStartIndex) return null
		
		return targetElement
	}
	
	override fun updateUI(p: Set<String>, context: ParameterInfoUIContext) {
		//PARAM1, PARAM2, ...
		//不高亮特定的参数
		val paramNames = p
		val text = paramNames.joinToString()
		val startOffset = 0
		val endOffset = 0
		context.setupUIComponentPresentation(text, startOffset, endOffset, false, false, false, context.defaultParameterColor)
	}
	
	override fun updateParameterInfo(parameterOwner: ParadoxScriptExpressionElement, context: UpdateParameterInfoContext) {
		context.parameterOwner = parameterOwner
	}
	
	override fun showParameterInfo(element: ParadoxScriptExpressionElement, context: CreateParameterInfoContext) {
		context.showHint(element, element.textRange.startOffset + 1, this)
	}
}