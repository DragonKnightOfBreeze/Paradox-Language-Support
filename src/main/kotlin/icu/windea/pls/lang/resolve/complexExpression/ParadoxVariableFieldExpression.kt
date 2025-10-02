package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxVariableFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

/**
 * 变量字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeGroups.ValueField]。
 * - 作为 [ParadoxValueFieldExpression] 的子集。相较之下，仅支持调用变量。
 *
 * 示例：
 * ```
 * root.owner.some_variable
 * ```
 */
interface ParadoxVariableFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val variableNode: ParadoxDataSourceNode

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression?
    }

    companion object : Resolver by ParadoxVariableFieldExpressionResolverImpl()
}
