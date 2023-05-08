package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.core.expression.nodes.ParadoxScriptValueArgumentExpressionNode
 */
class ParadoxInScriptValueExpressionParameterSupport: ParadoxDefinitionParameterSupport(){
    override fun findContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        val expressionElement = element.parentOfType<ParadoxScriptStringExpressionElement>() ?: return null
        val text = expressionElement.text
        val scriptValueExpressionOffsetInElement = text.indexOf("value:").let { if(it != -1) it + 6 else return null } //快速判断
        var scriptValueExpression: ParadoxScriptValueExpression? = null
        var configGroup: CwtConfigGroup? = null
        var file: PsiFile? = null
        when(from) {
            //extraArgs: config, completionOffset
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                
            }
            //extraArgs: file, offset
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                file = extraArgs.getOrNull(0)?.castOrNull<ParadoxScriptFile>() ?: return null
                val offset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: return null
                val offsetInExpression = offset - expressionElement.startOffset
                val pipeIndex = text.indexOf('|', scriptValueExpressionOffsetInElement)
                if(pipeIndex == -1 || pipeIndex >= offsetInExpression) return null //要求光标在管道符之后
                if(text.isLeftQuoted()) return null
                val config = ParadoxConfigHandler.getConfigs(expressionElement).firstOrNull() ?: return null
                if(!config.expression.type.isValueFieldType()) return null
                val textRange = TextRange.create(0, text.length)
                configGroup = config.info.configGroup
                val isKey = expressionElement is ParadoxScriptPropertyKey
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return null
                scriptValueExpression = valueFieldExpression.scriptValueExpression
            }
        }
        if(scriptValueExpression == null || configGroup == null) return null
        val rangeInElement = scriptValueExpression.scriptValueNode.rangeInExpression
        val definitionName = scriptValueExpression.scriptValueName ?: return null
        val argumentNames = scriptValueExpression.argumentNames
        val definitionTypes = listOf("script_value")
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val result = ParadoxParameterContextReferenceInfo(expressionElement.createPointer(file), rangeInElement, definitionName, argumentNames, gameType, project)
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
        //extraArgs: argumentNode
        val argumentNode = extraArgs.getOrNull(0)?.castOrNull<ParadoxScriptValueArgumentExpressionNode>() ?: return null
        val name = argumentNode.text
        val definitionName = argumentNode.scriptValueNode?.text ?: return null
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
