package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxVariableFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkValueNode

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
 *
 * ### 语法
 *
 * ```bnf
 * variable_field_expression ::= variable
 *                             | link ("." link)* "." variable
 *
 * private link ::= system_scope | scope | dynamic_scope_link
 * private dynamic_scope_link ::= scope_link_prefix? scope_link_value
 *                              | dynamic_scope_link_name "(" dynamic_scope_link_args? ")"
 * private dynamic_scope_link_args ::= dynamic_scope_link_arg ("," dynamic_scope_link_arg)*
 *
 * private variable ::= data_source
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由零个或多个“作用域链接”与一个“变量数据源”按 `.` 相连：`scope_link ('.' scope_link)* '.' variable`；也可仅含变量。
 * - 分段规则：按 `.` 切分；忽略参数文本与括号内的点；当进入括号后，仅在配对的 `)` 之后恢复 `.` 分段；`@` 和 `|` 在顶层作为屏障（其后不再继续 `.` 分段）。
 * - 在相邻节点之间会插入 `.` 运算符节点（[ParadoxOperatorNode]）。
 *
 * #### 节点组成
 * - 作用域链接：参见 [ParadoxScopeLinkNode]。
 * - 变量节点：末段解析为 [ParadoxDataSourceNode]，其数据源限定为 `linksOfVariable` 提供的变量链接配置。
 *
 * #### 备注
 * - 纯数字文本（整数/浮点）与带一元运算符前缀的参数会被解析器直接排除（非变量场景）。
 * - 若作用域链接段使用了带参数的动态链接（如 `prefix(x)`），支持其不是末段（例如：`prefix(x).owner.variable`）；参数的多项、空白与单引号字面量行为参见 [ParadoxScopeFieldExpression] 与 [ParadoxScopeLinkValueNode]。
 */
interface ParadoxVariableFieldExpression : ParadoxComplexExpression, ParadoxLinkedExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val variableNode: ParadoxDataSourceNode

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression?
    }

    companion object : Resolver by ParadoxVariableFieldExpressionResolverImpl()
}
