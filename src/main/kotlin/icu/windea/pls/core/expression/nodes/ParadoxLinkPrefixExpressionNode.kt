package icu.windea.pls.core.expression.nodes

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxLinkPrefixExpressionNode : ParadoxExpressionNode {
    val linkConfigs: List<CwtLinkConfig>
}
