package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.core.expression.nodes.ParadoxScriptValueArgumentExpressionNode
 */
class ParadoxInScriptValueExpressionParameterSupport: ParadoxDefinitionParameterSupport(){
    override fun findContextReferenceInfo(element: PsiElement, config: CwtDataConfig<*>?, from: ParadoxParameterContextReferenceInfo.From): ParadoxParameterContextReferenceInfo? {
        TODO("Not yet implemented")
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtDataConfig<*>?): ParadoxParameterElement? {
        if(rangeInElement == null || config != null) return null
        val expressionNodeKey = rangeInElement.toString() + "@" + ParadoxScriptValueArgumentExpressionNode::class.java.name
        val node = element.getUserData(PlsKeys.expressionNodesKey)?.get(expressionNodeKey)?.castOrNull<ParadoxScriptValueArgumentExpressionNode>() ?: return null
        val name = node.text
        val definitionName = node.scriptValueNode?.text ?: return null
        val configGroup = node.configGroup
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
