package icu.windea.pls.lang.resolve.complexExpression.nodes

import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig

interface ParadoxSystemScopeAwareLinkNode : ParadoxLinkNode {
    val config: CwtSystemScopeConfig
}
