package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
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
        return Reference(element, rangeInExpression, this)
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentExpressionNode {
            return ParadoxScriptValueArgumentExpressionNode(text, textRange, scriptValueNode, configGroup)
        }
    }
    
    /**
     * @see icu.windea.pls.lang.parameter.ParadoxScriptValueInlineParameterSupport
     */
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val node: ParadoxScriptValueArgumentExpressionNode
    ) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName))
        }
        
        override fun resolve(): ParadoxParameterElement? {
            val config = CwtConfigHandler.getConfigs(element, orDefault = false).firstOrNull() ?: return null
            return ParadoxParameterSupport.resolveArgument(element, rangeInElement, config)
        }
    }
}
