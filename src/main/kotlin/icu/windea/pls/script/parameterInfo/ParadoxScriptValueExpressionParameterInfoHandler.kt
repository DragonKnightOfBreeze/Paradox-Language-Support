package icu.windea.pls.script.parameterInfo

import com.intellij.lang.parameterInfo.*
import icu.windea.pls.script.psi.*

/**
 * 显示SV表达式的参数信息（如果有）。
 */
class ParadoxScriptValueExpressionParameterInfoHandler : ParameterInfoHandler<ParadoxScriptStringExpressionElement, Set<String>> {
	//光标位置需要位于带参数的SV表达式的参数部分中，即在value:xxx|xxx|xxx|的第一个管道符之后，第二个管道符之前
	
	override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParadoxScriptStringExpressionElement? {
		//val offset = context.offset
		//val element = context.file.findElementAt(offset) ?: return null
		//val targetElement = element.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return null
		//if(!targetElement.isExpressionElement()) return null
		//
		//val text = targetElement.text
		//if(!text.contains("value:") || !text.contains('|')) return null //快速判断
		//val config = resolveConfigs(targetElement).firstOrNull() ?: return null
		//val dataType = config.expression.type
		//if(dataType != CwtDataTypes.ValueField && dataType != CwtDataTypes.IntValueField) return null
		//val configGroup = config.info.configGroup
		//val valueFieldExpression = ParadoxScriptExpression.resolveValueField(text, configGroup)
		//val prefixInfo = valueFieldExpression.prefixInfo ?: return null
		//if(prefixInfo.text != "value:") return null
		//val dataSourceInfo = valueFieldExpression.dataSourceInfo ?: return null
		////要求光标位置在SV表达式中的参数部分中
		//if(offset - targetElement.textRange.startOffset <= valueFieldExpression.scriptValueParametersStartIndex) return null
		//
		////合并所有可能的参数名
		//val svName = dataSourceInfo.text
		//val selector = definitionSelector().gameTypeFrom(context.file).preferRootFrom(context.file)
		//val definitions = ParadoxDefinitionSearch.search(svName, "script_value", context.project, selector = selector).findAll()
		//val parameterNamesSet = definitions.mapNotNullTo(mutableSetOf()) { definition ->
		//	definition.parameterMap.keys.ifEmpty { setOf(PlsDocBundle.message("noParameters")) }
		//}
		//if(parameterNamesSet.isEmpty()) return null
		//context.itemsToShow = parameterNamesSet.toTypedArray()
		//
		//return targetElement
		return null
	}
	
	override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptStringExpressionElement? {
		//val offset = context.offset
		//val element = context.file.findElementAt(offset) ?: return null
		//val targetElement = element.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return null
		//if(!targetElement.isExpressionElement()) return null
		//val current = context.parameterOwner
		//if(current != null && current !== targetElement) return null
		//
		//val text = targetElement.text
		//if(!text.contains("value:") || !text.contains('|')) return null //快速判断
		//val config = resolveConfigs(targetElement).firstOrNull() ?: return null
		//val dataType = config.expression.type
		//if(dataType != CwtDataTypes.ValueField && dataType != CwtDataTypes.IntValueField) return null
		//val configGroup = config.info.configGroup
		//val valueFieldExpression = ParadoxScriptExpression.resolveValueField(text, configGroup)
		//val prefixInfo = valueFieldExpression.prefixInfo ?: return null
		//if(prefixInfo.text != "value:") return null
		//valueFieldExpression.dataSourceInfo ?: return null
		////要求光标位置在SV表达式中的参数部分中
		//if(offset - targetElement.textRange.startOffset <= valueFieldExpression.scriptValueParametersStartIndex) return null
		//
		//return targetElement
		return null
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
	
	override fun updateParameterInfo(parameterOwner: ParadoxScriptStringExpressionElement, context: UpdateParameterInfoContext) {
		context.parameterOwner = parameterOwner
	}
	
	override fun showParameterInfo(element: ParadoxScriptStringExpressionElement, context: CreateParameterInfoContext) {
		context.showHint(element, element.textRange.startOffset + 1, this)
	}
}
