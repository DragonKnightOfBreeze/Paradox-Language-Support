package icu.windea.pls.core.expression.nodes

import icu.windea.pls.config.cwt.config.*

sealed interface ParadoxLinkPrefixExpressionNode: ParadoxExpressionNode {
	val linkConfigs: List<CwtLinkConfig>
}
