package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.expression.complex.nodes.*

/**
 * 复杂表达式，对应脚本语言/本地化语言中的某段特定的标识符。
 *
 * （插件目前认为）复杂表达式不能用引号括起。
 */
interface ParadoxComplexExpression : ParadoxComplexExpressionNode {
    val errors: List<ParadoxComplexExpressionError>

    abstract class Base : ParadoxComplexExpression {
        override fun equals(other: Any?): Boolean {
            return this === other || (other is ParadoxComplexExpression && this.javaClass == other.javaClass && text == other.text)
        }

        override fun hashCode(): Int {
            return text.hashCode()
        }

        override fun toString(): String {
            return text
        }
    }

    companion object Resolver {
        fun resolveByConfig(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
            val dataType = config.expression?.type ?: return null
            return when {
                dataType in CwtDataTypeGroups.DynamicValue -> ParadoxDynamicValueExpression.resolve(expressionString, range, configGroup, config)
                dataType in CwtDataTypeGroups.ScopeField -> ParadoxScopeFieldExpression.resolve(expressionString, range, configGroup)
                dataType in CwtDataTypeGroups.ValueField -> ParadoxValueFieldExpression.resolve(expressionString, range, configGroup)
                dataType in CwtDataTypeGroups.VariableField -> ParadoxVariableFieldExpression.resolve(expressionString, range, configGroup)
                dataType == CwtDataTypes.DatabaseObject -> ParadoxDatabaseObjectExpression.resolve(expressionString, range, configGroup)
                dataType == CwtDataTypes.DefineReference -> ParadoxDefineReferenceExpression.resolve(expressionString, range, configGroup)
                else -> null
            }
        }
    }
}
