package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxCommandScopeNode: ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeNode {
            ParadoxParameterizedCommandScopeNode.resolve(text, textRange)?.let { return it }
            ParadoxDynamicCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxPredefinedCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandScopeNode(text, textRange)
        }
    }
}

