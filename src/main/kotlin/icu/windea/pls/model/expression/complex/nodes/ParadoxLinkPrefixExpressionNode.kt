package icu.windea.pls.model.expression.complex.nodes

import icu.windea.pls.config.config.*

sealed interface ParadoxLinkPrefixExpressionNode : ParadoxExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
