package icu.windea.pls.core.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.complex.errors.*
import icu.windea.pls.core.expression.complex.nodes.*

/**
 * 用于表达式脚本语言中的复杂表达式，对应匹配特定CWT规则类型的key或value（或者它们的特定部分）。
 * 目前认为不能用引号括起。
 */
interface ParadoxComplexExpression : ParadoxExpressionNode {
    val configGroup: CwtConfigGroup
    
    fun validate(): List<ParadoxExpressionError> = emptyList()
    
    fun complete(context: ProcessingContext, result: CompletionResultSet) = pass()
    
    companion object Resolver {
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? =
            doResolve(expression, range, configGroup, config)
    }
}

//Implementations

private fun doResolve(expression: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
    val dataType = config.expression?.type ?: return null
    return when {
        dataType in CwtDataTypeGroups.DynamicValue -> ParadoxDynamicValueExpression.resolve(expression, range, configGroup, config)
        dataType in CwtDataTypeGroups.ScopeField -> ParadoxScopeFieldExpression.resolve(expression, range, configGroup)
        dataType in CwtDataTypeGroups.ValueField -> ParadoxValueFieldExpression.resolve(expression, range, configGroup)
        dataType in CwtDataTypeGroups.VariableField -> ParadoxVariableFieldExpression.resolve(expression, range, configGroup)
        else -> null
    }
}
