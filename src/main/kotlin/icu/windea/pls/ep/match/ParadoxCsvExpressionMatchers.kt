package icu.windea.pls.ep.match

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
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.model.ParadoxType

class ParadoxBaseCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun matches(element: PsiElement, expressionText: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxMatchResult? {
        return when {
            configExpression.type == CwtDataTypes.Bool -> {
                val value = expressionText
                val r = ParadoxTypeResolver.isBoolean(value)
                ParadoxMatchResult.of(r)
            }
            configExpression.type == CwtDataTypes.Int -> {
                val value = expressionText
                val r = value.isEmpty() || ParadoxTypeResolver.isInt(value) // empty value is allowed
                if (!r) return ParadoxMatchResult.NotMatch
                run {
                    val intRange = configExpression.intRange ?: return@run
                    val intValue = value.toIntOrNull() ?: return@run
                    return ParadoxMatchResult.LazySimpleMatch { intRange.contains(intValue) }
                }
                ParadoxMatchResult.ExactMatch
            }
            configExpression.type == CwtDataTypes.Float -> {
                val value = expressionText
                val r = value.isEmpty() || ParadoxTypeResolver.isFloat(value) // empty value is allowed
                if (!r) return ParadoxMatchResult.NotMatch
                run {
                    val floatRange = configExpression.floatRange ?: return@run
                    val floatValue = value.toFloatOrNull() ?: return@run
                    return ParadoxMatchResult.LazySimpleMatch { floatRange.contains(floatValue) }
                }
                ParadoxMatchResult.ExactMatch
            }
            configExpression.type == CwtDataTypes.Scalar -> {
                ParadoxMatchResult.FallbackMatch // always match (fallback)
            }
            else -> null
        }
    }
}

class ParadoxCoreCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun matches(element: PsiElement, expressionText: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxMatchResult? {
        val project = configGroup.project
        val dataType = configExpression.type
        return when {
            dataType == CwtDataTypes.Definition -> {
                // can be an int or float here (e.g., for <technology_tier>)
                val value = expressionText.unquote()
                val valueType = ParadoxTypeResolver.resolve(value)
                if (valueType != ParadoxType.String && valueType != ParadoxType.Int && valueType != ParadoxType.Float) return ParadoxMatchResult.NotMatch
                if (!value.isIdentifier('.', '-')) return ParadoxMatchResult.NotMatch
                ParadoxMatchResultProvider.getDefinitionMatchResult(element, project, value, configExpression)
            }
            dataType == CwtDataTypes.EnumValue -> {
                val value = expressionText.unquote()
                val enumName = configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
                run {
                    // match simple enums
                    val enumConfig = configGroup.enums[enumName] ?: return@run
                    val r = value in enumConfig.values
                    return ParadoxMatchResult.of(r)
                }
                run {
                    // match complex enums
                    val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
                    // complexEnumValue的值必须合法
                    if (ParadoxComplexEnumValueManager.getName(value) == null) return ParadoxMatchResult.NotMatch
                    return ParadoxMatchResultProvider.getComplexEnumValueMatchResult(element, project, value, enumName, complexEnumConfig)
                }
                ParadoxMatchResult.NotMatch
            }
            else -> null
        }
    }
}
