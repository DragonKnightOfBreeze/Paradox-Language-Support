package icu.windea.pls.lang.resolving.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolving.complexExpression.impl.ParadoxComplexExpressionResolverImpl
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxComplexExpressionNode

/**
 * 复杂表达式。
 *
 * 对应脚本语言与本地化语言中的一段特定的表达式文本，它们可能包含数个节点，且允许嵌套包含。
 */
interface ParadoxComplexExpression : ParadoxComplexExpressionNode {
    val errors: List<ParadoxComplexExpressionError>

    fun validateAllNodes(errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean
    fun getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError>
    fun getAllReferences(element: ParadoxExpressionElement): List<PsiReference>

    interface Resolver {
        fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression?
        fun resolveByDataType(text: String, range: TextRange, configGroup: CwtConfigGroup, dataType: CwtDataType, config: CwtConfig<*>? = null): ParadoxComplexExpression?
        fun resolveByConfig(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression?
    }

    companion object : Resolver by ParadoxComplexExpressionResolverImpl()
}
