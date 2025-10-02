package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxScopeFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode

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
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由一个或多个“作用域链接”按 `.` 相连：`scope_link ('.' scope_link)*`。
 * - 分段规则：按 `.` 切分，忽略参数文本与括号内的点；当进入括号后，仅在配对的 `)` 之后恢复 `.` 分段；`@` 和 `|` 在顶层作为屏障（其后不再继续 `.` 分段）。
 *
 * #### 作用域链接类别
 * - [ParadoxSystemScopeNode]：来自 `system_scopes.cwt` 的预定义系统作用域，例如 `root`。
 * - [ParadoxScopeNode]：来自 `links.cwt` 的预定义作用域链接（`forScope()` 且 `fromData == false`），例如 `owner`。
 * - [ParadoxDynamicScopeLinkNode]：来自 `links.cwt` 的动态作用域链接（`fromData == true` 或 `fromArgument == true`）。
 *
 * #### 动态作用域链接形态
 * - 前缀形式：`<prefix><value>`（如 `event_target:<value>`）；前缀由 [ParadoxScopeLinkPrefixNode] 表示，值由 [ParadoxScopeLinkValueNode] 表示。
 * - 括号传参与形式：`<prefixWithoutColon>(<args>)`（如 `relations(<args>)`）；括号由 [ParadoxOperatorNode] 表示。
 * - 无前缀形式：仅 `<value>`；当链接允许无前缀时生效。
 *
 * #### 括号参数（仅动态链接 fromArgument）
 * - 允许不是链接中的最后一个节点（例如：`prefix(x).owner`）。
 * - 允许多个参数，以逗号分隔；括号与参数之间、参数与逗号之间的空白均被接受并保留为 [ParadoxBlankNode]。
 * - 允许用单引号包裹的字面量参数（例如：`'abc'`），将作为字面量处理，不做引用解析，对应地规则侧可使用 `data_source = scalar`。
 *
 * #### 值节点解析优先级
 * 1. 若链接的 `configExpression` 的类型属于 `ScopeField` 组，则值解析为单个作用域链接（[ParadoxScopeLinkNode]）。
 * 2. 否则若链接的 `dataSourceExpression` 的类型属于 `DynamicValue` 组，则解析为 [ParadoxDynamicValueExpression]。
 * 3. 否则解析为数据源节点 [ParadoxDataSourceNode]（引用变量/定义等）。
 *
 * #### 备注
 * - 解析过程中会在相邻链接节点之间插入 `.` 运算符节点（[ParadoxOperatorNode]）。
 * - 参数文本中的 `.` 不作为分段切分点。
 * - 若在下一处 `.` 之前出现 `@`、`|` 或 `(`（且均不在参数文本内），则停止继续切分，余下文本整体作为后续节点的输入。
 */
interface ParadoxScopeFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression?
    }

    companion object : Resolver by ParadoxScopeFieldExpressionResolverImpl()
}
