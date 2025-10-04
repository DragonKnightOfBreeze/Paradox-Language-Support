package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxCommandExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandSuffixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText

/**
 * （本地化）命令表达式。
 * - 可以在本地化文件中作为命令文本（[ParadoxLocalisationCommandText]）使用。
 * 示例：
 * ```
 * Root.GetName
 * Root.Owner.event_target:some_event_target.var
 * ```
 *
 * ### 语法
 *
 * ```bnf
 * command_expression ::= command_field command_suffix?
 *                      | link ("." link)* "." command_field command_suffix?
 *
 * private link ::= command_scope_link
 * private command_suffix ::= "&" suffix | "::" suffix
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由一个或多个“命令作用域链接”与一个“命令字段”按 `.` 相连，之后可选带后缀：
 * - 分段规则：按 `.` 切分；忽略参数文本与括号内的点；当进入括号后，仅在配对的 `)` 之后恢复 `.` 分段。
 *
 * #### 节点组成
 * - 作用域链接段：[ParadoxCommandScopeLinkNode]（按 `.` 切分的前若干段）。
 * - 字段段：[ParadoxCommandFieldNode]（最后一段）。
 * - 点分隔符：`.`（[ParadoxOperatorNode]，插入在相邻段之间）。
 * - 后缀：由分隔符（[ParadoxMarkerNode] 的 `&` 或 `::`）与 [ParadoxCommandSuffixNode] 组成（可选）。
 *
 * #### 动态命令作用域链接的括号参数
 * - 允许不是链接中的最后一个节点（例如：`Relations(x).GetName`）。
 * - 允许多个参数，使用逗号分隔并兼容多余空白；空白会被保留为 [ParadoxBlankNode]；单引号包裹的参数将作为字面量并以字符串样式高亮，详见 [ParadoxCommandScopeLinkValueNode]。
 */
interface ParadoxCommandExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression?
    }

    companion object : Resolver by ParadoxCommandExpressionResolverImpl()
}
