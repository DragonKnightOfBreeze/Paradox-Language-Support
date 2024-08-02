package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxScopeLinkNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeLinkNode {
            ParadoxParameterizedScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxSystemScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorScopeLinkNode(text, textRange, configGroup)
        }
    }
}

