package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxValueFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode

/**
 * 值字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeGroups.ValueField]。
 *
 * 示例：
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * root.owner.some_variable
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由零个或多个“作用域链接”与一个“值字段”按 `.` 相连：`scope_link ('.' scope_link)* '.' value_field`；也可仅含值字段。
 * - 分段规则：按 `.` 切分，但会忽略参数文本中的点；若在下一处 `.` 之前出现 `@`、`|` 或 `(`（且均不在参数文本内），则不再继续按 `.` 切分，余下文本作为单个节点交由后续解析。
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
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val valueFieldNode: ParadoxValueFieldNode
    val scriptValueExpression: ParadoxScriptValueExpression?

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression?
    }

    companion object : Resolver by ParadoxValueFieldExpressionResolverImpl()
}
