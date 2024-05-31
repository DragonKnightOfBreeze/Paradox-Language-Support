package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueArgumentNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val scriptValueNode: ParadoxScriptValueNode?,
    val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode {
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
        fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentNode {
            return ParadoxScriptValueArgumentNode(text, textRange, scriptValueNode, configGroup)
        }
    }
    
    /**
     * @see icu.windea.pls.ep.parameter.ParadoxScriptValueInlineParameterSupport
     */
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val node: ParadoxScriptValueArgumentNode
    ) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }
        
        override fun resolve(): ParadoxParameterElement? {
            val config = CwtConfigHandler.getConfigs(element, orDefault = false).firstOrNull() ?: return null
            return ParadoxParameterSupport.resolveArgument(element, rangeInElement, config)
        }
    }
}
