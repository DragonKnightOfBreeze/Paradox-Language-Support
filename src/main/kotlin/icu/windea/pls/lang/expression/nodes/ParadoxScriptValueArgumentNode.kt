package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.unquote
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

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
        if (reference == null) return null
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
