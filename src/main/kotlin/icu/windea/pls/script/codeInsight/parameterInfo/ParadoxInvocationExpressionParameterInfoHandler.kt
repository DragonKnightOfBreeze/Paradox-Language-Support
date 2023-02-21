package icu.windea.pls.script.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.api.impls.XmlParameterInfoHandler

/**
 * 显示调用子句中的参数信息（如果支持）。
 */
class ParadoxInvocationExpressionParameterInfoHandler : ParameterInfoHandler<ParadoxScriptProperty, Set<ParadoxParameterInfo>> {
    //向上找第一个scriptProperty，直到其作为子节点的scriptProperty可以匹配enum[scripted_effect_params]
    
    private fun findTargetElement(context: ParameterInfoContext): ParadoxScriptProperty? {
        val element = context.file.findElementAt(context.offset) ?: return null
        return element
            .parents(false)
            .filterIsInstance<ParadoxScriptProperty>()
            .find { prop ->
                prop.definitionMemberInfo
                    ?.takeUnless { it.isDefinition }
                    ?.getConfigs()
                    ?.any { config ->
                        config is CwtPropertyConfig && config.properties?.any { prop ->
                            prop.keyExpression.let { CwtConfigHandler.isParameter(prop) }
                        } ?: false
                    } ?: false
            }
            ?.takeIf { result ->
                //光标位置必须位于block中
                val block = result.propertyValue as? ParadoxScriptBlock ?: return@takeIf false
                block.textRange.let { range -> context.offset > range.startOffset && context.offset < range.endOffset }
            }
    }
    
    override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParadoxScriptProperty? {
        val targetElement = findTargetElement(context) ?: return null
        val definitionName = targetElement.name
        val config = ParadoxCwtConfigHandler.getPropertyConfigs(targetElement).firstOrNull() ?: return null
        val definitionType = config.keyExpression.value ?: return null
        //合并所有可能的参数名
        val selector = definitionSelector(project).gameTypeFrom(context.file).preferRootFrom(context.file)
        val definitions = ParadoxDefinitionSearch.search(definitionName, definitionType, selector = selector).findAll()
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
    
    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParadoxScriptProperty? {
        val targetElement = findTargetElement(context) ?: return null
        val current = context.parameterOwner
        if(current != null && current !== targetElement) return null
        return targetElement
    }
    
    override fun updateUI(parameterInfos: Set<ParadoxParameterInfo>, context: ParameterInfoUIContext) {
        //PARAM1, PARAM2, ...
        //不高亮特定的参数
        var isFirst = true
        val text = if(parameterInfos.isEmpty()) PlsDocBundle.message("noParameters") else buildString {
            for(info in parameterInfos) {
                if(isFirst) isFirst = false else append(", ")
                append(info.name)
                if(info.optional) append("?") //optional marker
            }
        }
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
