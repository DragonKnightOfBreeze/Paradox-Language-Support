package icu.windea.pls.lang.expression.complex.nodes

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
    override val configGroup: CwtConfigGroup,
    val valueNode: ParadoxScriptValueNode?
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        if (text.isEmpty()) return null
        return ParadoxScriptAttributesKeys.ARGUMENT_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (element !is ParadoxScriptStringExpressionElement) return null //unexpected
        if (valueNode == null) return null
        if (text.isEmpty()) return null
        val reference = valueNode.getReference(element)
        if (reference?.resolve() == null) return null //skip if script value cannot be resolved
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
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
            val config = ParadoxExpressionManager.getConfigs(element, orDefault = false).firstOrNull() ?: return null
            return ParadoxParameterSupport.resolveArgument(element, rangeInElement, config)
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, valueNode: ParadoxScriptValueNode?): ParadoxScriptValueArgumentNode {
            return ParadoxScriptValueArgumentNode(text, textRange, configGroup, valueNode)
        }
    }
}
