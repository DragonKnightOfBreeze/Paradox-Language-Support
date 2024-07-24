package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxScopeFieldNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldNode {
            ParadoxParameterizedScopeFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxSystemLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxScopeLinkFromDataNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorScopeFieldNode(text, textRange)
        }
    }
}

