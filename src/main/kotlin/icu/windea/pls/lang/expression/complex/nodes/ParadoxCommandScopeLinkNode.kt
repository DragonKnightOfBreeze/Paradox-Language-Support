package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxCommandScopeLinkNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeLinkNode {
            ParadoxSystemCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandScopeLinkNode(text, textRange, configGroup)
        }
    }
}

