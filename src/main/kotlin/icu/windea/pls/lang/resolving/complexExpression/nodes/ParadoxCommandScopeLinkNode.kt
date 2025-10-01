package icu.windea.pls.lang.resolving.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxCommandScopeLinkNode : ParadoxComplexExpressionNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeLinkNode {
            ParadoxSystemCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandScopeLinkNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}

