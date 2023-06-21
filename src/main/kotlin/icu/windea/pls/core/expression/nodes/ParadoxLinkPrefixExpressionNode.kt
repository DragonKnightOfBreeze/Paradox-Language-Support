package icu.windea.pls.core.expression.nodes

import icu.windea.pls.lang.cwt.config.*

sealed interface ParadoxLinkPrefixExpressionNode: ParadoxExpressionNode {
	val linkConfigs: List<CwtLinkConfig>
}
