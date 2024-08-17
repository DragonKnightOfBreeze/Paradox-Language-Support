package icu.windea.pls.lang.expression.complex.nodes

import icu.windea.pls.config.config.*

sealed interface ParadoxLinkPrefixNode : ParadoxComplexExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
