package icu.windea.pls.script.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 显示SV表达式的参数信息（如果有）。
 */
class ParadoxScriptValueExpressionParameterInfoHandler : ParameterInfoHandler<ParadoxScriptStringExpressionElement, Set<String>> {
	//光标位置需要位于带参数的SV表达式的参数部分中，即在value:xxx|xxx|xxx|的第一个管道符之后，第二个管道符之前
	
	private fun findTargetElement(context: ParameterInfoContext): Tuple2<ParadoxScriptStringExpressionElement?, String>? {
		val offset = context.offset
		val element = context.file.findElementAt(offset) ?: return null
		val targetElement = element.parentOfType<ParadoxScriptStringExpressionElement>() ?: return null
		if(!targetElement.isExpression()) return null
		val text = targetElement.text
		if(text.isLeftQuoted()) return null
		if(!text.contains("value:") || !text.contains('|')) return null //快速判断
		val configs = ParadoxCwtConfigHandler.resolveConfigs(targetElement, false, false)
		val config = configs.firstOrNull() ?: return null
		val dataType = config.expression.type
		if(dataType != CwtDataType.ValueField && dataType != CwtDataType.IntValueField) return null
		val configGroup = config.info.configGroup
		val textRange = TextRange.create(0, text.length)
		val isKey = element is ParadoxScriptPropertyKey
		val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
		if(valueFieldExpression == null) return null
		val valueLinkFromDataNode = valueFieldExpression.valueFieldNode.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
			?:return null
		val scriptValueExpression = valueLinkFromDataNode.dataSourceNode.nodes.findIsInstance<ParadoxScriptValueExpression>()
			?: return null
		val scriptValueExpressionNode = scriptValueExpression.scriptValueNode
		val firstParameterNode = scriptValueExpression.nodes.findIsInstance<ParadoxScriptValueParameterExpressionNode>()
			?: return null
		val argStartIndex = firstParameterNode.rangeInExpression.startOffset
		
		//要求光标位置在SV表达式中的参数部分中
		if(offset - targetElement.textRange.startOffset < argStartIndex) return null
		
		return targetElement to scriptValueExpressionNode.text
	}
	
	override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParadoxScriptStringExpressionElement? {
		val (targetElement, svName) = findTargetElement(context) ?: return null
		//合并所有可能的参数名
		val selector = definitionSelector().gameTypeFrom(context.file).preferRootFrom(context.file)
		val definitions = ParadoxDefinitionSearch.search(svName, "script_value", context.project, selector = selector).findAll()
		if(definitions.isEmpty()) return null
		val parameterNamesSet = definitions.mapNotNullTo(mutableSetOf()) { definition ->
			definition.parameterMap.keys.ifEmpty { setOf(PlsDocBundle.message("noParameters")) }
		}
		if(parameterNamesSet.isEmpty()) return null
		context.itemsToShow = parameterNamesSet.toTypedArray()

		return targetElement
	}
	
	override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptStringExpressionElement? {
		val (targetElement) = findTargetElement(context) ?: return null
		val current = context.parameterOwner
		if(current != null && current !== targetElement) return null
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
	
	override fun updateParameterInfo(parameterOwner: ParadoxScriptStringExpressionElement, context: UpdateParameterInfoContext) {
		context.parameterOwner = parameterOwner
	}
	
	override fun showParameterInfo(element: ParadoxScriptStringExpressionElement, context: CreateParameterInfoContext) {
		context.showHint(element, element.textRange.startOffset + 1, this)
	}
}
