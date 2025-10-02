package icu.windea.pls.lang.resolve.complexExpression.nodes

import icu.windea.pls.config.config.delegated.CwtLinkConfig

sealed interface ParadoxLinkPrefixNode : ParadoxComplexExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
