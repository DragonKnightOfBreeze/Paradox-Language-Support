package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxValueFieldNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldNode {
            ParadoxParameterizedValueFieldNode.resolve(text, textRange,configGroup)?.let { return it }
            ParadoxPredefinedValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorValueFieldNode(text, textRange, configGroup)
        }
    }
}
