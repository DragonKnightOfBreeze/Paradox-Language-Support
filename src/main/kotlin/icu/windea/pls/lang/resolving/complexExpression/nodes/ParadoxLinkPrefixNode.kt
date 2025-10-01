package icu.windea.pls.lang.resolving.complexExpression.nodes

import icu.windea.pls.config.config.delegated.CwtLinkConfig

sealed interface ParadoxLinkPrefixNode : ParadoxComplexExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
