package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.core.expression.ParadoxScriptValueExpression
 * @see icu.windea.pls.core.expression.nodes.ParadoxScriptValueArgumentExpressionNode
 */
class ParadoxScriptValueInlineParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement) = false
    
    override fun findContext(element: PsiElement) = null
    
    override fun resolveParameter(element: ParadoxParameter) = null
    
    override fun resolveConditionParameter(element: ParadoxConditionParameter) = null
    
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
        val expressionElementOffset = expressionElement.startOffset
        scriptValueExpression.nodes.forEach f@{ 
            if(it !is ParadoxScriptValueArgumentExpressionNode) return@f
            if(completionOffset != -1 && completionOffset in it.rangeInExpression.shiftRight(expressionElementOffset)) return@f
            val argumentName = it.text
            argumentNames.add(argumentName)
        }
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val info = ParadoxParameterContextReferenceInfo(expressionElement.createPointer(), contextName, argumentNames, contextNameRange, gameType, project)
        info.putUserData(ParadoxParameterSupport.Keys.definitionName, definitionName)
        info.putUserData(ParadoxParameterSupport.Keys.definitionTypes, definitionTypes)
        return info
    }
    
    override fun resolveArguments(element: ParadoxScriptExpressionElement): List<ParadoxParameterElement>? {
        TODO("Not yet implemented")
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(rangeInElement == null) return null
        if(config !is CwtMemberConfig<*>) return null
        if(!config.expression.type.isValueFieldType()) return null
        val expression = element.text.takeIf { it.contains("value:") } ?: return null
        val range = TextRange.create(0, expression.length)
        val configGroup = config.info.configGroup
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, range, configGroup) ?: return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
        val scriptValueNode = scriptValueExpression.scriptValueNode
        val definitionName = scriptValueNode.text
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = listOf("script_value")
        val argumentNode = scriptValueExpression.nodes.find f@{
            if(it !is ParadoxScriptValueArgumentExpressionNode) return@f false
            if(it.rangeInExpression != rangeInElement) return@f false
            true
        } as? ParadoxScriptValueArgumentExpressionNode ?: return null
        val name = argumentNode.text
        val contextName = definitionName
        val contextIcon = PlsIcons.Definition
        val key = "definition@$definitionName: script_value"
        val rangeInParent = rangeInElement
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, key, rangeInParent, readWriteAccess, gameType, project)
        result.putUserData(ParadoxParameterSupport.Keys.definitionName, definitionName)
        result.putUserData(ParadoxParameterSupport.Keys.definitionTypes, definitionTypes)
        return result
    }
    
    override fun processContext(element: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean) = false
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean) = false
}
