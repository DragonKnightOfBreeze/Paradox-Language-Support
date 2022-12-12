package icu.windea.pls.script.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolvePropertyConfigs
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.api.impls.XmlParameterInfoHandler

/**
 * 显示调用子句中的参数信息（如果支持）。
 */
class ParadoxInvocationExpressionParameterInfoHandler : ParameterInfoHandler<ParadoxScriptProperty, Set<String>> {
	//向上找第一个scriptProperty，直到其作为子节点的scriptProperty可以匹配enum[scripted_effect_params]
	
	private fun findTargetElement(context: ParameterInfoContext): ParadoxScriptProperty? {
		val element = context.file.findElementAt(context.offset) ?: return null
		val targetElement = element.parentOfType<ParadoxScriptStringExpressionElement>() ?: return null
		if(!targetElement.isExpressionElement()) return null
		return targetElement
			.parents(false)
			.filterIsInstance<ParadoxScriptProperty>()
			.find { prop ->
				prop.definitionElementInfo?.takeIf { it.isValid }?.getConfigs()?.any { config ->
					config is CwtPropertyConfig && config.properties?.any { prop ->
						prop.keyExpression.let { it.type == CwtDataTypes.Enum && it.value == CwtConfigHandler.paramsEnumName }
					} ?: false
				} ?: false
			}
			?.takeIf { result ->
				//光标位置必须位于block中
				result.propertyValue?.textRange?.let { r -> context.offset > r.startOffset && context.offset < r.endOffset } ?: false
			}
	}
	
	override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParadoxScriptProperty? {
		val targetElement = findTargetElement(context) ?: return null
		val definitionName = targetElement.name
		val config = resolvePropertyConfigs(targetElement).firstOrNull() ?: return null
		val definitionType = config.keyExpression.value ?: return null
		//合并所有可能的参数名
		val selector = definitionSelector().gameTypeFrom(context.file).preferRootFrom(context.file)
		val definitions = ParadoxDefinitionSearch.search(definitionName, definitionType, context.project, selector = selector).findAll()
		if(definitions.isEmpty()) return null
		val parameterNamesSet = definitions.mapNotNullTo(mutableSetOf()) { definition ->
			definition.parameterMap.keys.ifEmpty { setOf(PlsDocBundle.message("noParameters")) }
		}
		if(parameterNamesSet.isEmpty()) return null
		context.itemsToShow = parameterNamesSet.toTypedArray()
		return targetElement
	}
	
	override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptProperty? {
		val targetElement = findTargetElement(context) ?: return null
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
	
	override fun updateParameterInfo(parameterOwner: ParadoxScriptProperty, context: UpdateParameterInfoContext) {
		context.parameterOwner = parameterOwner
	}
	
	override fun showParameterInfo(element: ParadoxScriptProperty, context: CreateParameterInfoContext) {
		context.showHint(element, element.textRange.startOffset + 1, this)
	}
}
