package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueArgumentExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val scriptValueNode: ParadoxScriptValueExpressionNode?,
    val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
    override fun getAttributesKey(): TextAttributesKey? {
        if(text.isEmpty()) return null
        return ParadoxScriptAttributesKeys.ARGUMENT_KEY
    }
    
    override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
        if(scriptValueNode == null) return null
        if(text.isEmpty()) return null
        val reference = scriptValueNode.getReference(element)
        if(reference?.resolve() == null) return null //skip if script value cannot be resolved
        val expressionNodeKey = rangeInExpression.toString() + "@" + ParadoxScriptValueArgumentExpressionNode::class.java.name
        element.getOrPutUserData(PlsKeys.expressionNodesKey) { mutableMapOf() }.put(expressionNodeKey, this)
        return Reference(element, rangeInExpression)
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentExpressionNode {
            return ParadoxScriptValueArgumentExpressionNode(text, textRange, scriptValueNode, configGroup)
        }
    }
    
    /**
     * @see icu.windea.pls.lang.parameter.impl.ParadoxInScriptValueExpressionParameterSupport
     */
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange
    ) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.value, newElementName))
        }
        
        override fun resolve(): ParadoxParameterElement? {
            return ParadoxParameterSupport.resolveArgument(element, rangeInElement, null)
        }
    }
}
