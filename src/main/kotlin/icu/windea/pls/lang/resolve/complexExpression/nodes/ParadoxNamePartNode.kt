package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression

/**
 * 命名格式表达式（[ParadoxNameFormatExpression]）中的命名部件节点。即 `{<x>}` 中的 `<x>`。
 */
class ParadoxNamePartNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode>
) : ParadoxComplexExpressionNodeBase()
