package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxScopeFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

/**
 * 作用域字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeGroups.ScopeField]。
 *
 * 示例：
 * ```
 * root
 * root.owner
 * event_target:some_target
 * ```
 *
 * 语法：
 * ```bnf
 * scope_field_expression ::= scope +
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN // predefined by CWT Config (see system_scopes.cwt)
 * scope_link ::= TOKEN // predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_value // predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN // e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_value ::= expression // e.g. "some_variable" while the link's data source is "value[variable]"
 * expression ::= data_source | dynamic_value_expression // see: ParadoxDataSourceNode, ParadoxDynamicValueExpression
 * ```
 */
interface ParadoxScopeFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression?
    }

    companion object : Resolver by ParadoxScopeFieldExpressionResolverImpl()
}
