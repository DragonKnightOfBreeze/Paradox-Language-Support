package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxValueFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldValueNode

/**
 * 值字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeSets.ValueField]。
 *
 * 示例：
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * relations(root)
 * root.owner.some_variable
 * ```
 *
 * 语法：
 * ```bnf
 * value_field_expression ::= (scope_link ".")* value_field
 * scope_link ::= system_scope | scope | dynamic_scope_link | parameterized_scope_link
 * dynamic_scope_link ::= scope_link_with_prefix | scope_link_with_args
 * private scope_link_with_prefix ::= scope_link_prefix? scope_link_value
 * private scope_link_with_args ::= scope_link_prefix "(" scope_link_args ")"
 * private scope_link_args ::= scope_link_arg ("," scope_link_arg)* // = scope_link_value
 * private scope_link_arg ::= scope_link_value
 * scope_link_value ::= scope_link | dynamic_value_expression | data_source
 * value_field ::= predefined_value_field | dynamic_value_field | parameterized_value_field
 * dynamic_value_field ::= value_field_with_prefix | value_field_with_args
 * private value_field_with_prefix ::= value_field_prefix? value_field_value
 * private value_field_with_args ::= value_field_prefix "(" value_field_args ")"
 * private value_field_args ::= value_field_arg ("," value_field_arg)* // = value_field_value
 * private value_field_arg ::= value_field_value
 * value_field_value ::= dynamic_value_expression | scope_field_expression | script_value_expression | data_source
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由零个或多个“作用域链接”与一个“值字段”按 `.` 相连：`scope_link ('.' scope_link)* '.' value_field`；也可仅含值字段。
 * - 分段规则：按 `.` 切分；忽略参数文本与括号内的点；当进入括号后，仅在配对的 `)` 之后恢复 `.` 分段；`@` 和 `|` 在顶层作为屏障（其后不再继续 `.` 分段）。
 * - 在相邻节点之间会插入 `.` 运算符节点（[ParadoxOperatorNode]）。
 *
 * #### 节点组成
 * - 作用域链接：参见 [ParadoxScopeLinkNode]（语义等同于 `ParadoxScopeFieldExpression` 的分段部分）。
 * - 值字段节点：参见 [ParadoxValueFieldNode]（末段）。
 *
 * #### 值字段节点的类别
 * - 预定义值字段：[ParadoxPredefinedValueFieldNode]（来自 `links.cwt`，`forValue()` 且 `fromData == false`）。
 * - 动态值字段：[ParadoxDynamicValueFieldNode]（`fromData == true` 或 `fromArgument == true`）。
 * - 参数化值字段：[ParadoxParameterizedValueFieldNode]（文本为参数形式）。
 *
 * #### 动态值字段的内部形态
 * - 前缀形式：`<prefix><value>`；前缀由 [ParadoxValueFieldPrefixNode] 表示，值由 [ParadoxValueFieldValueNode] 表示。
 * - 括号传参与形式：`<prefixWithoutColon>(<value>)`；括号由 [ParadoxOperatorNode] 表示。
 * - 无前缀形式：仅 `<value>`；当链接允许无前缀时生效。
 *
 * #### 值字段值节点的解析优先级
 * 1. 若链接的数据源类型属于 `DynamicValue` 组，则解析为 [ParadoxDynamicValueExpression]。
 * 2. 否则若链接的数据源类型属于 `ScopeField` 组，则解析为 [ParadoxScopeFieldExpression]。
 * 3. 否则：
 *    - 若文本包含 `|` 且可匹配脚本值配置（名称为 `script_value`），解析为 [ParadoxScriptValueExpression]；
 *    - 否则解析为 [ParadoxDataSourceNode]（变量/定义等数据源）。
 *
 * #### 备注（括号参数兼容性）
 * - 若作用域链接段使用了带参数的动态链接（如 `prefix(x)`），支持其不是末段（例如：`prefix(x).owner`）。
 * - 允许多个参数，逗号分隔并兼容多余空白；空白被保留为 [ParadoxBlankNode]；单引号字面量参数将作为字面量处理并以字符串样式高亮（详见 [ParadoxScopeLinkValueNode] 的实现）。
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression, ParadoxLinkedExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val valueFieldNode: ParadoxValueFieldNode
    val scriptValueExpression: ParadoxScriptValueExpression?

    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxValueFieldExpression?
    }

    companion object : Resolver by ParadoxValueFieldExpressionResolverImpl()
}
