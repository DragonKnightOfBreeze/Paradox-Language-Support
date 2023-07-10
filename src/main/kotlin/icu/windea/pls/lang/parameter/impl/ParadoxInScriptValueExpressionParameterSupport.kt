package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.core.expression.nodes.ParadoxScriptValueArgumentExpressionNode
 */
class ParadoxInScriptValueExpressionParameterSupport : ParadoxDefinitionParameterSupport() {
    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var expressionElement: ParadoxScriptStringExpressionElement? = null
        var text: String? = null
        var expressionElementConfig: CwtMemberConfig<*>? = null
        var completionOffset = -1
        when(from) {
            //extraArgs: config, completionOffset
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                text = expressionElement.text ?: return null
                if(text.isLeftQuoted()) return null
                if(!text.contains("value:")) return null //快速判断
                expressionElementConfig = config
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                text = expressionElement.text ?: return null
                if(text.isLeftQuoted()) return null
                if(!text.contains("value:")) return null //快速判断
                expressionElementConfig = contextConfig
            }
            //extraArgs: offset
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val offset = extraArgs.getOrNull(0)?.castOrNull<Int>() ?: return null
                expressionElement = element.parentOfType<ParadoxScriptStringExpressionElement>() ?: return null
                text = expressionElement.text ?: return null
                if(text.isLeftQuoted()) return null
                if(!text.contains("value:")) return null //快速判断
                val pipeIndex = text.indexOf('|', text.indexOf("value:").let { if(it != -1) it + 6 else return null })
                if(pipeIndex == -1 || pipeIndex >= offset - expressionElement.startOffset) return null //要求光标在管道符之后
                expressionElementConfig = ParadoxConfigHandler.getConfigs(expressionElement).firstOrNull() ?: return null
            }
        }
        if(!expressionElementConfig.expression.type.isValueFieldType()) return null
        val textRange = TextRange.create(0, text.length)
        val configGroup = expressionElementConfig.info.configGroup
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup) ?: return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
        val definitionName = scriptValueExpression.scriptValueNode.text.takeIfNotEmpty() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = listOf("script_value")
        val contextName = definitionName
        val argumentNames = mutableSetOf<String>()
        val contextNameRange = scriptValueExpression.scriptValueNode.rangeInExpression //text range of script value name
        val argumentRanges = mutableListOf<Tuple3<String, TextRange, TextRange?>>()
        val expressionElementOffset = expressionElement.startOffset
        scriptValueExpression.argumentNodes.forEach f@{ (nameNode, valueNode) ->
            if(completionOffset != -1 && completionOffset in nameNode.rangeInExpression.shiftRight(expressionElementOffset)) return@f
            val argumentName = nameNode.text
            argumentNames.add(argumentName)
            argumentRanges.add(tupleOf(argumentName, nameNode.rangeInExpression, valueNode?.rangeInExpression))
        }
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val result = ParadoxParameterContextReferenceInfo(expressionElement.createPointer(), contextName, argumentNames, contextNameRange, argumentRanges, gameType, project)
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
        //extraArgs: argumentNode
        val argumentNode = extraArgs.getOrNull(0)?.castOrNull<ParadoxScriptValueArgumentExpressionNode>() ?: return null
        val name = argumentNode.text
        val definitionName = argumentNode.scriptValueNode?.text ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val configGroup = argumentNode.configGroup
        val contextKey = "definition@$definitionName: script_value"
        val definitionTypes = listOf("script_value")
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val result = ParadoxParameterElement(element, name, definitionName, contextKey, readWriteAccess, gameType, project)
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        return result
    }
}
