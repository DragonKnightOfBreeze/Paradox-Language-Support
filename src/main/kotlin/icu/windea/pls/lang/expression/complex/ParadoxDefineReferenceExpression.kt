package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.expression.complex.nodes.*

/**
 * 定义引用表达式。对应的CWT规则类型为[CwtDataTypes.DefineReference]。
 *
 * 语法：
 *
 * ```bnf
 * database_object_expression ::= database_object_type ":" database_object (":" swapped_database_object)?
 * database_object_type ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
 * database_object ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
 * swapped_database_object ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
 * ```
 *
 * 示例：
 *
 * * `define:NPortrait|GRACEFUL_AGING_START`
 */
class ParadoxDefineReferenceExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    override val errors by lazy { validate() }
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression? {
            if (expressionString.isEmpty()) return null

            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxDefineReferenceExpression(expressionString, range, nodes, configGroup)
            ////TODO 1.3.25
            return expression
        }

        private fun ParadoxDefineReferenceExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            var malformed = false
            for (node in nodes) {
                if (node.text.isEmpty()) {
                    malformed = true
                    break
                }
            }
            if (malformed) {
                errors += ParadoxComplexExpressionErrors.malformedDefineReferenceExpression(rangeInExpression, text)
            }
            return errors.pinned { it.isMalformedError() }
        }
    }
}
