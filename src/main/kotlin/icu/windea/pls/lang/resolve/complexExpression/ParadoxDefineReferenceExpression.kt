package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxDefineReferenceExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode

/**
 * 预设值引用表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DefineReference]。
 *
 * 示例：
 * ```
 * define:NPortrait|GRACEFUL_AGING_START
 * ```
 *
 * 语法：
 * ```bnf
 * define_reference_expression ::= "define:" define_namespace "|" define_variable
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 固定以前缀 `define:` 开头，随后是命名空间与变量名，以 `|` 分隔：`define:<namespace>|<variable>`。
 *
 * #### 节点组成
 * - 命名空间：[ParadoxDefineNamespaceNode]（`common/defines` 下 .txt 的一级键）。
 * - 变量名：[ParadoxDefineVariableNode]（同文件的二级键）。
 */
interface ParadoxDefineReferenceExpression : ParadoxComplexExpression {
    val namespaceNode: ParadoxDefineNamespaceNode?
    val variableNode: ParadoxDefineVariableNode?

    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression?
    }

    companion object : Resolver by ParadoxDefineReferenceExpressionResolverImpl()
}
