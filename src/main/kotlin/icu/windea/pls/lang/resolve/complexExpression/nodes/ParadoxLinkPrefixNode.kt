package icu.windea.pls.lang.resolve.complexExpression.nodes

import icu.windea.pls.config.config.delegated.CwtLinkConfig

interface ParadoxLinkPrefixNode : ParadoxComplexExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
