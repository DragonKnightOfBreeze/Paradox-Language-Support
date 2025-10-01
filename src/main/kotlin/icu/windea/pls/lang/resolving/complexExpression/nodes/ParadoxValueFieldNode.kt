package icu.windea.pls.lang.resolving.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxValueFieldNode : ParadoxComplexExpressionNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldNode {
            ParadoxPredefinedValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedValueFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorValueFieldNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
