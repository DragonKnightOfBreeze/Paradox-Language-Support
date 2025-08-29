package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxValueFieldNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldNode {
            ParadoxPredefinedValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorValueFieldNode(text, textRange, configGroup)
        }
    }
}
