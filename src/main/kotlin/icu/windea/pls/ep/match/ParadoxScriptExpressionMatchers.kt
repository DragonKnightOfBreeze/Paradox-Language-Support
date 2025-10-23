package icu.windea.pls.ep.match

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.match.ParadoxMatchManager
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.members
import icu.windea.pls.script.psi.propertyValue

class ParadoxBaseScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        return when (configExpression.type) {
            CwtDataTypes.Block -> {
                if (expression.isKey != false) return ParadoxMatchResult.NotMatch
                if (expression.type != ParadoxType.Block) return ParadoxMatchResult.NotMatch
                if (config !is CwtMemberConfig) return ParadoxMatchResult.NotMatch

                val blockElement = when (element) {
                    is ParadoxScriptProperty -> element.propertyValue()
                    is ParadoxScriptBlock -> element
                    else -> null
                } ?: return ParadoxMatchResult.NotMatch

                // 如果存在子句规则内容为空，则仅当子句内容为空时才认为匹配
                if (config.configs.isNullOrEmpty()) {
                    if (blockElement.members().none()) return ParadoxMatchResult.ExactMatch
                    return ParadoxMatchResult.FallbackMatch
                }

                ParadoxMatchResult.LazyBlockAwareMatch p@{
                    val keys = ParadoxExpressionManager.getInBlockKeys(config)
                    if (keys.isEmpty()) return@p true

                    // 根据其中存在的属性键进行过滤（注意这里需要考虑内联和可选的情况）
                    // 如果子句中包含对应的任意子句规则中的任意必须的属性键（忽略大小写），则认为匹配
                    val actualKeys = mutableSetOf<String>()
                    blockElement.members().options(conditional = true, inline = true).forEach {
                        if (it is ParadoxScriptProperty) actualKeys.add(it.name)
                    }
                    actualKeys.any { it in keys }
                }
            }
            CwtDataTypes.Bool -> {
                val r = expression.type.isBooleanType()
                ParadoxMatchResult.of(r)
            }
            CwtDataTypes.Int -> {
                // quoted number (e.g., "1") -> ok according to vanilla game files
                val value = expression.value
                val r = expression.type.isIntType() || ParadoxTypeResolver.resolve(value).isIntType()
                if (!r) return ParadoxMatchResult.NotMatch
                run {
                    val intRange = configExpression.intRange ?: return@run
                    val intValue = value.toIntOrNull() ?: return@run
                    return ParadoxMatchResult.LazySimpleMatch { intRange.contains(intValue) }
                }
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.Float -> {
                // quoted number (e.g., "1.0") -> ok according to vanilla game files
                val value = expression.value
                val r = expression.type.isFloatType() || ParadoxTypeResolver.resolve(value).isFloatType()
                if (!r) return ParadoxMatchResult.NotMatch
                run {
                    val floatRange = configExpression.floatRange ?: return@run
                    val floatValue = value.toFloatOrNull() ?: return@run
                    return ParadoxMatchResult.LazySimpleMatch { floatRange.contains(floatValue) }
                }
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.Scalar -> {
                val r = when {
                    expression.isKey == true -> true // key -> ok
                    expression.type == ParadoxType.Parameter -> true // parameter -> ok
                    expression.type.isBooleanType() -> true // boolean -> sadly, also ok for compatibility
                    expression.type.isIntType() -> true // number -> ok according to vanilla game files
                    expression.type.isFloatType() -> true // number -> ok according to vanilla game files
                    expression.type.isStringType() -> true // unquoted/quoted string -> ok
                    else -> false
                }
                ParadoxMatchResult.ofFallback(r)
            }
            CwtDataTypes.ColorField -> {
                val r = expression.type.isColorType() && configExpression.value?.let { expression.value.startsWith(it) } != false
                ParadoxMatchResult.of(r)
            }
            CwtDataTypes.Any -> {
                ParadoxMatchResult.FallbackMatch
            }
            else -> null
        }
    }
}

class ParadoxCoreScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        val project = configGroup.project
        val dataType = configExpression.type
        return when (dataType) {
            CwtDataTypes.PercentageField -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                val r = ParadoxTypeResolver.isPercentageField(expression.value)
                ParadoxMatchResult.of(r)
            }
            CwtDataTypes.DateField -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                val datePattern = configExpression.value
                val r = ParadoxTypeResolver.isDateField(expression.value, datePattern)
                ParadoxMatchResult.of(r)
            }
            CwtDataTypes.Definition, CwtDataTypes.SuffixAwareDefinition -> {
                // can be an int or float here (e.g., for <technology_tier>)
                if (!expression.type.isStringType() && expression.type != ParadoxType.Int && expression.type != ParadoxType.Float) return ParadoxMatchResult.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-')) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResultProvider.getDefinitionMatchResult(element, project, expression.value, configExpression)
            }
            CwtDataTypes.Localisation, CwtDataTypes.SuffixAwareLocalisation -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResultProvider.getLocalisationMatchResult(element, project, expression.value, configExpression)
            }
            CwtDataTypes.SyncedLocalisation, CwtDataTypes.SuffixAwareSyncedLocalisation -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResultProvider.getSyncedLocalisationMatchResult(element, project, expression.value, configExpression)
            }
            CwtDataTypes.InlineLocalisation -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.quoted) return ParadoxMatchResult.FallbackMatch // "quoted_string" -> any string
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResultProvider.getLocalisationMatchResult(element, project, expression.value, configExpression)
            }
            CwtDataTypes.AbsoluteFilePath -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                ParadoxMatchResult.ExactMatch
            }
            in CwtDataTypeGroups.PathReference -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResultProvider.getPathReferenceMatchResult(element, project, expression.value, configExpression)
            }
            CwtDataTypes.EnumValue -> {
                if (expression.type.isBlockLikeType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val name = expression.value
                val enumName = configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
                // match simple enums
                val enumConfig = configGroup.enums[enumName]
                if (enumConfig != null) {
                    val r = name in enumConfig.values
                    return ParadoxMatchResult.of(r)
                }
                // match complex enums
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if (complexEnumConfig != null) {
                    // complexEnumValue的值必须合法
                    if (ParadoxComplexEnumValueManager.getName(expression.value) == null) return ParadoxMatchResult.NotMatch
                    return ParadoxMatchResultProvider.getComplexEnumValueMatchResult(element, project, name, enumName, complexEnumConfig)
                }
                ParadoxMatchResult.NotMatch
            }
            in CwtDataTypeGroups.DynamicValue -> {
                if (expression.type.isBlockLikeType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                // dynamicValue的值必须合法
                val name = ParadoxDynamicValueManager.getName(expression.value) ?: return ParadoxMatchResult.NotMatch
                if (!name.isIdentifier('.')) return ParadoxMatchResult.NotMatch
                val dynamicValueType = configExpression.value
                if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
                ParadoxMatchResult.FallbackMatch
            }
            in CwtDataTypeGroups.ScopeField -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.value, textRange, configGroup)
                if (scopeFieldExpression == null) return ParadoxMatchResult.NotMatch
                if (scopeFieldExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
                ParadoxMatchResultProvider.getScopeFieldMatchResult(element, scopeFieldExpression, configExpression, configGroup)
            }
            in CwtDataTypeGroups.ValueField -> {
                // 也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if (dataType == CwtDataTypes.ValueField) {
                    if (expression.type.isFloatType() || ParadoxTypeResolver.resolve(expression.value).isFloatType()) return ParadoxMatchResult.ExactMatch
                } else if (dataType == CwtDataTypes.IntValueField) {
                    if (expression.type.isIntType() || ParadoxTypeResolver.resolve(expression.value).isIntType()) return ParadoxMatchResult.ExactMatch
                }

                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.value, textRange, configGroup)
                if (valueFieldExpression == null) return ParadoxMatchResult.NotMatch
                if (valueFieldExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
                ParadoxMatchResult.ExactMatch
            }
            in CwtDataTypeGroups.VariableField -> {
                // 也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if (dataType == CwtDataTypes.VariableField) {
                    if (expression.type.isFloatType() || ParadoxTypeResolver.resolve(expression.value).isFloatType()) return ParadoxMatchResult.ExactMatch
                } else if (dataType == CwtDataTypes.IntVariableField) {
                    if (expression.type.isIntType() || ParadoxTypeResolver.resolve(expression.value).isIntType()) return ParadoxMatchResult.ExactMatch
                }

                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.value, textRange, configGroup)
                if (variableFieldExpression == null) return ParadoxMatchResult.NotMatch
                if (variableFieldExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.Modifier -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (!expression.value.isParameterAwareIdentifier()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResultProvider.getModifierMatchResult(element, expression.value, configGroup)
            }
            CwtDataTypes.SingleAliasRight -> {
                ParadoxMatchResult.NotMatch // 不在这里处理
            }
            CwtDataTypes.AliasKeysField -> {
                if (!expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val aliasName = configExpression.value ?: return ParadoxMatchResult.NotMatch
                val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expression.value, expression.quoted, aliasName, configGroup, options) ?: return ParadoxMatchResult.NotMatch
                ParadoxScriptExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(aliasSubName, true), null, configGroup, ParadoxMatchOptions.Default)
            }
            CwtDataTypes.AliasName -> {
                if (!expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val aliasName = configExpression.value ?: return ParadoxMatchResult.NotMatch
                val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expression.value, expression.quoted, aliasName, configGroup, options) ?: return ParadoxMatchResult.NotMatch
                ParadoxScriptExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(aliasSubName, true), null, configGroup, ParadoxMatchOptions.Default)
            }
            CwtDataTypes.AliasMatchLeft -> {
                return ParadoxMatchResult.NotMatch // 不在这里处理
            }
            CwtDataTypes.Parameter -> {
                // 匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if (!expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
                if (!expression.value.isParameterAwareIdentifier()) return ParadoxMatchResult.NotMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.ParameterValue -> {
                // 匹配参数值（只要不是子句即可匹配）
                if (expression.type == ParadoxType.Block) return ParadoxMatchResult.NotMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.LocalisationParameter -> {
                // 匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if (!expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
                if (!expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.DatabaseObject -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expression.value, textRange, configGroup)
                if (databaseObjectExpression == null) return ParadoxMatchResult.NotMatch
                if (databaseObjectExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.DefineReference -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val defineReferenceExpression = ParadoxDefineReferenceExpression.resolve(expression.value, textRange, configGroup)
                if (defineReferenceExpression == null) return ParadoxMatchResult.NotMatch
                if (defineReferenceExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.StellarisNameFormat -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                val textRange = TextRange.create(0, expression.value.length)
                val stellarisNameFormatExpression = StellarisNameFormatExpression.resolve(expression.value, textRange, configGroup, config ?: return ParadoxMatchResult.NotMatch)
                if (stellarisNameFormatExpression == null) return ParadoxMatchResult.NotMatch
                if (stellarisNameFormatExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
                ParadoxMatchResult.ExactMatch
            }
            CwtDataTypes.ShaderEffect -> {
                // TODO 1.2.2+ 暂时作为一般的字符串处理
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResult.FallbackMatch
            }
            CwtDataTypes.TechnologyWithLevel -> {
                if (!expression.type.isStringType()) return ParadoxMatchResult.NotMatch
                if (expression.value.length > 1 && expression.value.indexOf('@') >= 1) return ParadoxMatchResult.ExactMatch
                if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
                ParadoxMatchResult.NotMatch
            }
            else -> null
        }
    }
}

class ParadoxConstantScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        if (configExpression.type != CwtDataTypes.Constant) return null
        val value = configExpression.value ?: return ParadoxMatchResult.NotMatch
        if (!configExpression.isKey) {
            // 常量的值也可能是yes/no
            val text = expression.value
            if ((value == "yes" || value == "no") && text.isLeftQuoted()) return ParadoxMatchResult.NotMatch
        }
        // 这里也用来匹配空字符串
        val r = expression.matchesConstant(value)
        return ParadoxMatchResult.of(r)
    }
}

class ParadoxTemplateScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        if (configExpression.type != CwtDataTypes.TemplateExpression) return null
        if (!expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
        if (expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        // 允许用引号括起
        return ParadoxMatchResultProvider.getTemplateMatchResult(element, expression.value, configExpression, configGroup)
    }
}

class ParadoxAntScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        if (configExpression.type != CwtDataTypes.Ant) return null
        val pattern = configExpression.value ?: return ParadoxMatchResult.NotMatch
        val ignoreCase = configExpression.ignoreCase ?: false
        val r = expression.value.matchesAntPattern(pattern, ignoreCase)
        return ParadoxMatchResult.of(r)
    }
}

class ParadoxRegexScriptExpressionMatcher : PatternAwareParadoxScriptExpressionMatcher() {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        if (configExpression.type != CwtDataTypes.Regex) return null
        val pattern = configExpression.value ?: return ParadoxMatchResult.NotMatch
        val ignoreCase = configExpression.ignoreCase ?: false
        val r = expression.value.matchesRegex(pattern, ignoreCase)
        return ParadoxMatchResult.of(r)
    }
}

class ParadoxPredicateBasedScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxScriptExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): ParadoxMatchResult? {
        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        // 这里的 config 也可能是属性值对应的规则，因此下面需要传入 config.memberConfig
        val memberConfig = if (config is CwtMemberConfig<*>) config.memberConfig else null
        if (memberConfig == null) return null
        if (!ParadoxMatchManager.matchesByPredicate(element, memberConfig)) return ParadoxMatchResult.NotMatch
        return null
    }
}
