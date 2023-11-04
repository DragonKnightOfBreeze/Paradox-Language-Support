package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxComplexExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*

/**
 * 用于表达式脚本语言中的复杂表达式，对应匹配特定CWT规则类型的key或value（或者它们的特定部分）。
 * 目前认为不能用引号括起。
 */
interface ParadoxComplexExpression : ParadoxExpressionNode {
    val configGroup: CwtConfigGroup
    
    fun validate(): List<ParadoxExpressionError> = emptyList()
    
    fun complete(context: ProcessingContext, result: CompletionResultSet) = pass()
    
    companion object Resolver
}

fun Resolver.resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
    val dataType = config.expression?.type ?: return null
    return when {
        dataType.isValueSetValueType() -> ParadoxValueSetValueExpression.resolve(expression, range, configGroup, config)
        dataType.isScopeFieldType() -> ParadoxScopeFieldExpression.resolve(expression, range, configGroup)
        dataType.isValueFieldType() -> ParadoxValueFieldExpression.resolve(expression, range, configGroup)
        dataType.isVariableFieldType() -> ParadoxVariableFieldExpression.resolve(expression, range, configGroup)
        else -> null
    }
}
