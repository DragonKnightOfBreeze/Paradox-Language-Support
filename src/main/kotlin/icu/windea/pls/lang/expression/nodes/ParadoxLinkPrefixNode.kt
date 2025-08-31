package icu.windea.pls.lang.expression.nodes

import icu.windea.pls.config.config.delegated.CwtLinkConfig

sealed interface ParadoxLinkPrefixNode : ParadoxComplexExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
