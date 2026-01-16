package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxScriptValueArgumentNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val valueNode: ParadoxScriptValueNode?
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        if (text.isEmpty()) return null
        return ParadoxScriptAttributesKeys.ARGUMENT_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (element !is ParadoxScriptStringExpressionElement) return null // unexpected
        if (valueNode == null) return null
        if (text.isEmpty()) return null
        val reference = valueNode.getReference(element)
        if (reference == null) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement)
    }

    /**
     * @see icu.windea.pls.ep.resolve.parameter.ParadoxScriptValueInlineParameterSupport
     */
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange
    ) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }

        override fun resolve(): ParadoxParameterElement? {
            val config = ParadoxConfigManager.getConfigs(element, orDefault = false).firstOrNull() ?: return null
            return ParadoxParameterService.resolveArgument(element, rangeInElement, config)
        }

        override fun canResolveFor(constraint: ParadoxResolveConstraint): Boolean {
            return when (constraint) {
                ParadoxResolveConstraint.Parameter -> true
                else -> false
            }
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, valueNode: ParadoxScriptValueNode?): ParadoxScriptValueArgumentNode {
            return ParadoxScriptValueArgumentNode(text, textRange, configGroup, valueNode)
        }
    }

    companion object : Resolver()
}
