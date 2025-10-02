package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys

/**
 * 字面量字符串节点（用于括号参数中的单引号包裹文本）。
 */
class ParadoxStringLiteralNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        // 使用本地化的 STRING_KEY 高亮以统一字符串样式
        return ParadoxLocalisationAttributesKeys.STRING_KEY
    }
}
