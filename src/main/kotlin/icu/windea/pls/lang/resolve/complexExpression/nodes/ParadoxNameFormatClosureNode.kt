package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression

/**
 * 命名格式表达式（[ParadoxNameFormatExpression]）中的闭包节点。即 `{...}`。
 */
class ParadoxNameFormatClosureNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode>
) : ParadoxComplexExpressionNodeBase()

