package icu.windea.pls.ep.expression

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Result
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class BaseParadoxScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        return when {
            configExpression.type == CwtDataTypes.Block -> {
                if (expression.isKey != false) return Result.NotMatch
                if (expression.type != ParadoxType.Block) return Result.NotMatch
                if (config !is CwtMemberConfig) return Result.NotMatch

                //* 如果子句中包含对应的任意子句规则中的任意必须的键（忽略大小写），则认为匹配
                //* 如果存在子句规则内容为空，则仅当子句内容为空时才认为匹配
                //* 不同的子句规则可以拥有部分相同的propertyKey

                val blockElement = when {
                    element is ParadoxScriptProperty -> element.propertyValue()
                    element is ParadoxScriptBlock -> element
                    else -> null
                } ?: return Result.NotMatch
                if (config.configs.isNullOrEmpty()) {
                    if (blockElement.isEmpty) return Result.ExactMatch
                    return Result.FallbackMatch
                }
                Result.LazyBlockAwareMatch p@{
                    val keys = ParadoxExpressionManager.getInBlockKeys(config)
                    if (keys.isEmpty()) return@p true
                    val actualKeys = mutableSetOf<String>()
                    //注意这里需要考虑内联和可选的情况
                    blockElement.processMember(conditional = true, inline = true) {
                        if (it is ParadoxScriptProperty) actualKeys.add(it.name)
                        true
                    }
                    actualKeys.any { it in keys }
                }
            }
            configExpression.type == CwtDataTypes.Bool -> {
                val r = expression.type.isBooleanType()
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.Int -> {
                //quoted number (e.g., "1") -> ok according to vanilla game files
                if (expression.type.isIntType() || ParadoxTypeManager.resolve(expression.value).isIntType()) {
                    val (min, max) = configExpression.intRange ?: return Result.ExactMatch
                    return Result.LazySimpleMatch p@{
                        val value = expression.value.toIntOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.Float -> {
                //quoted number (e.g., "1") -> ok according to vanilla game files
                if (expression.type.isFloatType() || ParadoxTypeManager.resolve(expression.value).isFloatType()) {
                    val (min, max) = configExpression.floatRange ?: return Result.ExactMatch
                    return Result.LazySimpleMatch p@{
                        val value = expression.value.toFloatOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.Scalar -> {
                val r = when {
                    expression.isKey == true -> true //key -> ok
                    expression.type == ParadoxType.Parameter -> true //parameter -> ok
                    expression.type.isBooleanType() -> true //boolean -> sadly, also ok for compatibility
                    expression.type.isIntType() -> true //number -> ok according to vanilla game files
                    expression.type.isFloatType() -> true //number -> ok according to vanilla game files
                    expression.type.isStringType() -> true //unquoted/quoted string -> ok
                    else -> false
                }
                Result.ofFallback(r)
            }
            configExpression.type == CwtDataTypes.ColorField -> {
                val r = expression.type.isColorType() && configExpression.value?.let { expression.value.startsWith(it) } != false
                Result.of(r)
            }
            else -> null
        }
    }
}

class CoreParadoxScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        val project = configGroup.project
        return when {
            configExpression.type == CwtDataTypes.PercentageField -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                val r = ParadoxTypeManager.isPercentageField(expression.value)
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.DateField -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                val r = ParadoxTypeManager.isDateField(expression.value)
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.Localisation -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                ParadoxExpressionMatcher.Impls.getLocalisationMatchResult(element, expression, project)
            }
            configExpression.type == CwtDataTypes.SyncedLocalisation -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                ParadoxExpressionMatcher.Impls.getSyncedLocalisationMatchResult(element, expression, project)
            }
            configExpression.type == CwtDataTypes.InlineLocalisation -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.quoted) return Result.FallbackMatch //"quoted_string" -> any string
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                ParadoxExpressionMatcher.Impls.getSyncedLocalisationMatchResult(element, expression, project)
            }
            configExpression.type == CwtDataTypes.Definition -> {
                //can be an integer here (e.g., for <technology_tier>)
                if (!expression.type.isStringType() && expression.type != ParadoxType.Int) return Result.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-')) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                ParadoxExpressionMatcher.Impls.getDefinitionMatchResult(element, expression, configExpression, project)
            }
            configExpression.type == CwtDataTypes.AbsoluteFilePath -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                Result.ExactMatch
            }
            configExpression.type in CwtDataTypeGroups.PathReference -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                ParadoxExpressionMatcher.Impls.getPathReferenceMatchResult(element, expression, configExpression, project)
            }
            configExpression.type == CwtDataTypes.EnumValue -> {
                if (expression.type.isBlockLikeType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val name = expression.value
                val enumName = configExpression.value ?: return Result.NotMatch //invalid cwt config
                //match simple enums
                val enumConfig = configGroup.enums[enumName]
                if (enumConfig != null) {
                    val r = name in enumConfig.values
                    return Result.of(r)
                }
                //match complex enums
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if (complexEnumConfig != null) {
                    //complexEnumValue的值必须合法
                    if (ParadoxComplexEnumValueManager.getName(expression.value) == null) return Result.NotMatch
                    return ParadoxExpressionMatcher.Impls.getComplexEnumValueMatchResult(element, name, enumName, complexEnumConfig, project)
                }
                Result.NotMatch
            }
            configExpression.type in CwtDataTypeGroups.DynamicValue -> {
                if (expression.type.isBlockLikeType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                //dynamicValue的值必须合法
                val name = ParadoxDynamicValueManager.getName(expression.value) ?: return Result.NotMatch
                if (!name.isIdentifier('.')) return Result.NotMatch
                val dynamicValueType = configExpression.value
                if (dynamicValueType == null) return Result.NotMatch
                Result.FallbackMatch
            }
            configExpression.type in CwtDataTypeGroups.ScopeField -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.value, textRange, configGroup)
                if (scopeFieldExpression == null) return Result.NotMatch
                if (scopeFieldExpression.getAllErrors(null).isNotEmpty()) return Result.PartialMatch
                ParadoxExpressionMatcher.Impls.getScopeFieldMatchResult(element, scopeFieldExpression, configExpression, configGroup)
            }
            configExpression.type in CwtDataTypeGroups.ValueField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if (configExpression.type == CwtDataTypes.ValueField) {
                    if (expression.type.isFloatType() || ParadoxTypeManager.resolve(expression.value).isFloatType()) return Result.ExactMatch
                } else if (configExpression.type == CwtDataTypes.IntValueField) {
                    if (expression.type.isIntType() || ParadoxTypeManager.resolve(expression.value).isIntType()) return Result.ExactMatch
                }

                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.value, textRange, configGroup)
                if (valueFieldExpression == null) return Result.NotMatch
                if (valueFieldExpression.getAllErrors(null).isNotEmpty()) return Result.PartialMatch
                Result.ExactMatch
            }
            configExpression.type in CwtDataTypeGroups.VariableField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if (configExpression.type == CwtDataTypes.VariableField) {
                    if (expression.type.isFloatType() || ParadoxTypeManager.resolve(expression.value).isFloatType()) return Result.ExactMatch
                } else if (configExpression.type == CwtDataTypes.IntVariableField) {
                    if (expression.type.isIntType() || ParadoxTypeManager.resolve(expression.value).isIntType()) return Result.ExactMatch
                }

                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.value, textRange, configGroup)
                if (variableFieldExpression == null) return Result.NotMatch
                if (variableFieldExpression.getAllErrors(null).isNotEmpty()) return Result.PartialMatch
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.DatabaseObject -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expression.value, textRange, configGroup)
                if (databaseObjectExpression == null) return Result.NotMatch
                if (databaseObjectExpression.getAllErrors(null).isNotEmpty()) return Result.PartialMatch
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.DefineReference -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val defineReferenceExpression = ParadoxDefineReferenceExpression.resolve(expression.value, textRange, configGroup)
                if (defineReferenceExpression == null) return Result.NotMatch
                if (defineReferenceExpression.getAllErrors(null).isNotEmpty()) return Result.PartialMatch
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.Modifier -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (!expression.value.isParameterAwareIdentifier()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                ParadoxExpressionMatcher.Impls.getModifierMatchResult(element, expression, configGroup)
            }
            configExpression.type == CwtDataTypes.SingleAliasRight -> {
                Result.NotMatch //不在这里处理
            }
            configExpression.type == CwtDataTypes.AliasKeysField -> {
                if (!expression.type.isStringLikeType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return Result.NotMatch
                val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expression.value, expression.quoted, aliasName, configGroup, options) ?: return Result.NotMatch
                ParadoxExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(aliasSubName, true), null, configGroup)
            }
            configExpression.type == CwtDataTypes.AliasName -> {
                if (!expression.type.isStringLikeType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return Result.NotMatch
                val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expression.value, expression.quoted, aliasName, configGroup, options) ?: return Result.NotMatch
                ParadoxExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(aliasSubName, true), null, configGroup)
            }
            configExpression.type == CwtDataTypes.AliasMatchLeft -> {
                return Result.NotMatch //不在这里处理
            }
            configExpression.type == CwtDataTypes.Any -> {
                Result.FallbackMatch
            }
            configExpression.type == CwtDataTypes.Parameter -> {
                //匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if (!expression.type.isStringLikeType()) return Result.NotMatch
                if (!expression.value.isParameterAwareIdentifier()) return Result.NotMatch
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.ParameterValue -> {
                //匹配参数值（只要不是子句即可匹配）
                if (expression.type == ParadoxType.Block) return Result.NotMatch
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.LocalisationParameter -> {
                //匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if (!expression.type.isStringLikeType()) return Result.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return Result.NotMatch
                Result.ExactMatch
            }
            configExpression.type == CwtDataTypes.ShaderEffect -> {
                //TODO 1.2.2+ 暂时作为一般的字符串处理
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                Result.FallbackMatch
            }
            configExpression.type == CwtDataTypes.StellarisNameFormat -> {
                //TODO 1.2.2+ 需要考虑进一步的支持
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                Result.FallbackMatch
            }
            configExpression.type == CwtDataTypes.TechnologyWithLevel -> {
                if (!expression.type.isStringType()) return Result.NotMatch
                if (expression.value.length > 1 && expression.value.indexOf('@') >= 1) return Result.ExactMatch
                if (expression.isParameterized()) return Result.ParameterizedMatch
                Result.NotMatch
            }
            else -> null
        }
    }
}

class TemplateExpressionParadoxScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        if (configExpression.type != CwtDataTypes.TemplateExpression) return null
        if (!expression.type.isStringLikeType()) return Result.NotMatch
        if (expression.isParameterized()) return Result.ParameterizedMatch
        //允许用引号括起
        return ParadoxExpressionMatcher.Impls.getTemplateMatchResult(element, expression, configExpression, configGroup)
    }
}

class AntExpressionParadoxScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        if (configExpression.type != CwtDataTypes.AntExpression) return null
        val pattern = configExpression.value ?: return Result.NotMatch
        val ignoreCase = configExpression.ignoreCase ?: false
        val r = expression.value.matchesAntPattern(pattern, ignoreCase)
        return Result.of(r)
    }
}

class RegexParadoxScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        if (configExpression.type != CwtDataTypes.Regex) return null
        val pattern = configExpression.value ?: return Result.NotMatch
        val ignoreCase = configExpression.ignoreCase ?: false
        val r = expression.value.matchesRegex(pattern, ignoreCase)
        return Result.of(r)
    }
}

class ConstantParadoxScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Result? {
        if (configExpression.type != CwtDataTypes.Constant) return null
        val value = configExpression.value ?: return Result.NotMatch
        if (!configExpression.isKey) {
            //常量的值也可能是yes/no
            val text = expression.value
            if ((value == "yes" || value == "no") && text.isLeftQuoted()) return Result.NotMatch
        }
        //这里也用来匹配空字符串
        val r = expression.matchesConstant(value)
        return Result.of(r)
    }
}
