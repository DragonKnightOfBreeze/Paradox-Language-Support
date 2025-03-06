package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.editor.*

class ParadoxValueFieldPrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base(), ParadoxLinkPrefixNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        val resolved = linkConfigs.mapNotNull { it.pointer.element?.bindConfig(it) }
        return Reference(element, rangeInElement, resolved)
    }

    class Reference(element: ParadoxExpressionElement, rangeInElement: TextRange, resolved: List<CwtProperty>) :
        PsiResolvedPolyVariantReference<CwtProperty>(element, rangeInElement, resolved)

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxValueFieldPrefixNode {
            return ParadoxValueFieldPrefixNode(text, textRange, configGroup, linkConfigs)
        }
    }
}
