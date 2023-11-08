package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxScopeFieldExpressionNode : ParadoxExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpressionNode {
            ParadoxSystemLinkExpressionNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxScopeLinkExpressionNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxScopeLinkFromDataExpressionNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedScopeFieldExpressionNode.resolve(text, textRange)?.let { return it }
            return ParadoxErrorScopeFieldExpressionNode(text, textRange)
        }
    }
}