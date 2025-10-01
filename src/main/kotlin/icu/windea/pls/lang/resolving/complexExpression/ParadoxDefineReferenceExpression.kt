package icu.windea.pls.lang.resolving.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolving.complexExpression.impl.ParadoxDefineReferenceExpressionResolverImpl
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxDefineVariableNode

/**
 * 预设值引用表达式。对应的规则类型为 [icu.windea.pls.config.CwtDataTypes.DefineReference]。
 *
 * 语法：
 * ```bnf
 * define_reference_expression ::= "define:" define_namespace "|" define_variable
 * define_namespace ::= TOKEN // level 1 property keys in .txt files in common/defines
 * define_variable ::= TOKEN // level 2 property keys in .txt files in common/defines
 * ```
 *
 * 示例：
 * ```
 * define:NPortrait|GRACEFUL_AGING_START
 * ```
 *
 * @see icu.windea.pls.config.CwtDataTypes.DefineReference
 */
interface ParadoxDefineReferenceExpression : ParadoxComplexExpression {
    val namespaceNode: ParadoxDefineNamespaceNode?
    val variableNode: ParadoxDefineVariableNode?

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression?
    }

    companion object : Resolver by ParadoxDefineReferenceExpressionResolverImpl()
}
