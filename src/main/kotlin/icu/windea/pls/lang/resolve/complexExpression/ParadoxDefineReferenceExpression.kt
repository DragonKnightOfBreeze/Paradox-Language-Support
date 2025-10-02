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
 * define_namespace ::= TOKEN // level 1 property keys in .txt files in common/defines
 * define_variable ::= TOKEN // level 2 property keys in .txt files in common/defines
 * ```
 */
interface ParadoxDefineReferenceExpression : ParadoxComplexExpression {
    val namespaceNode: ParadoxDefineNamespaceNode?
    val variableNode: ParadoxDefineVariableNode?

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression?
    }

    companion object : Resolver by ParadoxDefineReferenceExpressionResolverImpl()
}
