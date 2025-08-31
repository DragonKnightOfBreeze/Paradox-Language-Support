package icu.windea.pls.lang.expression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.impl.ParadoxComplexExpressionResolverImpl
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 复杂表达式。
 *
 * 复杂对应脚本语言与本地化语言中的一段特定的标识符，它们可能包含数个节点，且允许嵌套包含。
 */
interface ParadoxComplexExpression : ParadoxComplexExpressionNode {
    val errors: List<ParadoxComplexExpressionError>

    interface Resolver {
        fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression?
        fun resolveByDataType(text: String, range: TextRange, configGroup: CwtConfigGroup, dataType: CwtDataType, config: CwtConfig<*>? = null): ParadoxComplexExpression?
        fun resolveByConfig(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression?
    }

    companion object : Resolver by ParadoxComplexExpressionResolverImpl()
}
