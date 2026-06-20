package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.editor.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 数字字面量节点。
 */
class ParadoxNumberLiteralNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticHighlighterColors.number(element.language)
    }
}
