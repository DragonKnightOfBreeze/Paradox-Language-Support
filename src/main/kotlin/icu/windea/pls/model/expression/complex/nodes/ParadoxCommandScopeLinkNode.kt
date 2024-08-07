package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxCommandScopeLinkNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeLinkNode {
            ParadoxParameterizedCommandScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxSystemCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandScopeLinkNode(text, textRange, configGroup)
        }
    }
}

