package icu.windea.pls.model.expression.complex.nodes

import icu.windea.pls.config.config.*

sealed interface ParadoxLinkPrefixNode : ParadoxComplexExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
