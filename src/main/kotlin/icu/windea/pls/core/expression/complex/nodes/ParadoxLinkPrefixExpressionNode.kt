package icu.windea.pls.core.expression.complex.nodes

import icu.windea.pls.config.config.*
import icu.windea.pls.config.*

sealed interface ParadoxLinkPrefixExpressionNode : ParadoxExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
