package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxScopeFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode

/**
 * 作用域字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeSets.ScopeField]。
 * - 由多个作用域链接节点（[ParadoxScopeLinkNode]）组成。之间用点号分隔。
 * - 作用域链接节点可以是静态链接（[ParadoxSystemScopeNode] 和 [ParadoxScopeNode]）、动态链接（[ParadoxDynamicScopeLinkNode]）或者带参数的链接（[ParadoxParameterizedScopeLinkNode]）。
 * - 动态链接可能是前缀形式（`prefix:ds`），也可能是传参形式（`prefix(x)`）。其中可能嵌套其他复杂表达式。
 * - 对于传参形式的动态链接，兼容多个传参（`prefix(x,y)`）和字面量传参（`prefix('s')`）。传入链式表达式时，需要整个用双引号括起。
 *
 * [ParadoxDynamicScopeLinkNode] 的数据源的解析优先级：
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.DynamicValue]，则解析为 [ParadoxDynamicValueExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ScopeField]，则解析为 [ParadoxScopeFieldExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ValueField]，则解析为 [ParadoxValueFieldExpression]。
 * - 如果不是任何嵌套的复杂表达式，则解析为 [ParadoxDataSourceNode]。
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
 * scope_field_expression ::= (scope_link ".")* scope_link
 * scope_link ::= system_scope | scope | dynamic_scope_link | parameterized_scope_link
 * dynamic_scope_link ::= scope_link_with_prefix | scope_link_with_args
 * private scope_link_with_prefix ::= scope_link_prefix? scope_link_value
 * private scope_link_with_args ::= scope_link_prefix "(" scope_link_args ")"
 * private scope_link_args ::= scope_link_arg ("," scope_link_arg)* // = scope_link_value
 * private scope_link_arg ::= scope_link_value
 * scope_link_value ::= dynamic_value_expression | scope_field_expression | value_field_expression | data_source
 * ```
 */
interface ParadoxScopeFieldExpression : ParadoxComplexExpression, ParadoxLinkedExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>

    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression?
    }

    companion object : Resolver by ParadoxScopeFieldExpressionResolverImpl()
}
