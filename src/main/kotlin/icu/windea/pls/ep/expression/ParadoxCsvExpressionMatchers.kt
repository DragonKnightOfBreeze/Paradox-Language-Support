package icu.windea.pls.ep.expression

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Result
import icu.windea.pls.model.ParadoxType

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
                val r = value.isEmpty() || ParadoxTypeResolver.isInt(value) // empty value is allowed
                if (!r) return Result.NotMatch
                run {
                    val intRange = configExpression.intRange ?: return@run
                    val intValue = value.toIntOrNull() ?: return@run
                    return Result.LazySimpleMatch { intRange.contains(intValue) }
                }
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.Float -> {
                val value = expressionText
                val r = value.isEmpty() || ParadoxTypeResolver.isFloat(value) // empty value is allowed
                if (!r) return Result.NotMatch
                run {
                    val floatRange = configExpression.floatRange ?: return@run
                    val floatValue = value.toFloatOrNull() ?: return@run
                    return Result.LazySimpleMatch { floatRange.contains(floatValue) }
                }
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.Scalar -> {
                Result.FallbackMatch // always match (fallback)
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
                // can be an int or float here (e.g., for <technology_tier>)
                val value = expressionText.unquote()
                val valueType = ParadoxTypeResolver.resolve(value)
                if (valueType != ParadoxType.String && valueType != ParadoxType.Int && valueType != ParadoxType.Float) return Result.NotMatch
                if (!value.isIdentifier('.', '-')) return Result.NotMatch
                ParadoxExpressionMatcher.getDefinitionMatchResult(element, project, value, configExpression)
            }
            dataType == CwtDataTypes.EnumValue -> {
                val value = expressionText.unquote()
                val enumName = configExpression.value ?: return Result.NotMatch // invalid cwt config
                run {
                    // match simple enums
                    val enumConfig = configGroup.enums[enumName] ?: return@run
                    val r = value in enumConfig.values
                    return Result.of(r)
                }
                run {
                    // match complex enums
                    val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
                    // complexEnumValue的值必须合法
                    if (ParadoxComplexEnumValueManager.getName(value) == null) return Result.NotMatch
                    return ParadoxExpressionMatcher.getComplexEnumValueMatchResult(element, project, value, enumName, complexEnumConfig)
                }
                Result.NotMatch
            }
            else -> null
        }
    }
}
