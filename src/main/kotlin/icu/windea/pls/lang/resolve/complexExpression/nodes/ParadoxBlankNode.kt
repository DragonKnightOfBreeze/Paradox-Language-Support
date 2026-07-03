package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 空白节点（仅表示空白字符序列）。
 */
class ParadoxBlankNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase()
