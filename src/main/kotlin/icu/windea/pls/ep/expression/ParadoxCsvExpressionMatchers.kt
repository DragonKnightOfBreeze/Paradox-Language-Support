package icu.windea.pls.ep.expression

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configExpression.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Result
import icu.windea.pls.model.*

class BaseParadoxCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun matches(element: PsiElement, expressionText: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result? {
        return when {
            configExpression.type == CwtDataTypes.Bool -> {
                val value = expressionText
                val r = ParadoxTypeResolver.isBoolean(value)
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.Int -> {
                val value = expressionText
                val r = value.isEmpty() || ParadoxTypeResolver.isInt(value) //empty value is allowed
                if (!r) return Result.NotMatch
                run {
                    val (min, max) = configExpression.intRange ?: return@run
                    return Result.LazySimpleMatch {
                        val intValue = value.toIntOrNull() ?: 0
                        (min == null || min <= intValue) && (max == null || max >= intValue)
                    }
                }
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.Float -> {
                val value = expressionText
                val r = value.isEmpty() || ParadoxTypeResolver.isFloat(value) //empty value is allowed
                if (!r) return Result.NotMatch
                run {
                    val (min, max) = configExpression.floatRange ?: return@run
                    return Result.LazySimpleMatch {
                        val floatValue = value.toFloatOrNull() ?: 0f
                        (min == null || min <= floatValue) && (max == null || max >= floatValue)
                    }
                }
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.Scalar -> {
                Result.FallbackMatch //always match (fallback)
            }
            else -> null
        }
    }
}

class CoreParadoxCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun matches(element: PsiElement, expressionText: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result? {
        val project = configGroup.project
        val dataType = configExpression.type
        return when {
            dataType == CwtDataTypes.Definition -> {
                //can be an int or float here (e.g., for <technology_tier>)
                val value = expressionText.unquote()
                val valueType = ParadoxTypeResolver.resolve(value)
                if (valueType != ParadoxType.String && valueType != ParadoxType.Int && valueType != ParadoxType.Float) return Result.NotMatch
                if (!value.isIdentifier('.', '-')) return Result.NotMatch
                ParadoxExpressionMatcher.getDefinitionMatchResult(element, value, configExpression, project)
            }
            dataType == CwtDataTypes.EnumValue -> {
                val value = expressionText.unquote()
                val enumName = configExpression.value ?: return Result.NotMatch //invalid cwt config
                run {
                    //match simple enums
                    val enumConfig = configGroup.enums[enumName] ?: return@run
                    val r = value in enumConfig.values
                    return Result.of(r)
                }
                run {
                    //match complex enums
                    val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
                    //complexEnumValue的值必须合法
                    if (ParadoxComplexEnumValueManager.getName(value) == null) return Result.NotMatch
                    return ParadoxExpressionMatcher.getComplexEnumValueMatchResult(element, value, enumName, complexEnumConfig, project)
                }
                Result.NotMatch
            }
            else -> null
        }
    }
}
