package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxVariableFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

/**
 * 变量字段表达式。
 *
 * ### 说明
 * - 对应的规则数据类型为 [CwtDataTypeGroups.ValueField]。
 * - 作为 [ParadoxValueFieldExpression] 的子集。相较之下，仅支持调用变量。
 *
 * ### 示例
 * ```kotlin
 * root.owner.some_variable
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由零个或多个“作用域链接”与一个“变量数据源”按 `.` 相连：`scope_link ('.' scope_link)* '.' variable`；也可仅含变量。
 * - 分段规则：按 `.` 切分，但会忽略参数文本中的点；若在下一处 `.` 之前出现 `@`、`|` 或 `(`（且均不在参数文本内），则不再继续按 `.` 切分，余下文本作为单个节点交由后续解析。
 * - 在相邻节点之间会插入 `.` 运算符节点（[ParadoxOperatorNode]）。
 *
 * #### 节点组成
 * - 作用域链接：参见 [ParadoxScopeLinkNode]。
 * - 变量节点：末段解析为 [ParadoxDataSourceNode]，其数据源限定为 `linksOfVariable` 提供的变量链接配置。
 *
 * ### 备注
 * - 纯数字文本（整数/浮点）与带一元运算符前缀的参数会被解析器直接排除（非变量场景）。
 */
interface ParadoxVariableFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val variableNode: ParadoxDataSourceNode

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression?
    }

    companion object : Resolver by ParadoxVariableFieldExpressionResolverImpl()
}
