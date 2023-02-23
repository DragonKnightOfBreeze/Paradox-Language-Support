package icu.windea.pls.script.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 显示SV表达式的参数信息（如果有）。
 */
class ParadoxScriptValueExpressionParameterInfoHandler : ParameterInfoHandler<ParadoxScriptStringExpressionElement, Set<ParadoxParameterInfo>> {
    //光标位置需要位于带参数的SV表达式的参数部分中，即在value:xxx|xxx|xxx|的第一个管道符之后，第二个管道符之前
    
    private fun findTargetElement(context: ParameterInfoContext): Tuple2<ParadoxScriptStringExpressionElement?, String>? {
        val offset = context.offset
        val element = context.file.findElementAt(offset) ?: return null
        val targetElement = element.parentOfType<ParadoxScriptStringExpressionElement>() ?: return null
        if(!targetElement.isExpression()) return null
        val text = targetElement.text
        if(text.isLeftQuoted()) return null
        if(!text.contains("value:") || !text.contains('|')) return null //快速判断
        val configs = ParadoxCwtConfigHandler.getConfigs(targetElement, false, false)
        val config = configs.firstOrNull() ?: return null
        val dataType = config.expression.type
        if(!dataType.isValueFieldType()) return null
        val configGroup = config.info.configGroup
        val textRange = TextRange.create(0, text.length)
        val isKey = element is ParadoxScriptPropertyKey
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
        if(valueFieldExpression == null) return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
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
        val project = context.project
        val selector = definitionSelector(project, context.file).contextSensitive()
        val definitions = ParadoxDefinitionSearch.search(svName, "script_value", selector).findAll()
        if(definitions.isEmpty()) return null
        val parameterInfosMap = mutableMapOf<String, Collection<ParadoxParameterInfo>>()
        for(definition in definitions) {
            val parameters = definition.parameters
            if(parameters.isEmpty()) continue
            parameterInfosMap.putIfAbsent(parameters.keys.toString(), parameters.values)
        }
        if(parameterInfosMap.isEmpty()) return null
        context.itemsToShow = parameterInfosMap.values.toTypedArray()
        return targetElement
    }
    
    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptStringExpressionElement? {
        val (targetElement) = findTargetElement(context) ?: return null
        val current = context.parameterOwner
        if(current != null && current !== targetElement) return null
        return targetElement
    }
    
    override fun updateUI(infos: Set<ParadoxParameterInfo>, context: ParameterInfoUIContext) {
        //PARAM1, PARAM2, ...
        //不高亮特定的参数
        var isFirst = true
        val text = if(infos.isEmpty()) PlsDocBundle.message("noParameters") else buildString {
            for(info in infos) {
                if(isFirst) isFirst = false else append(", ")
                append(info.name)
                if(info.optional) append("?") //optional marker
            }
        }
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
