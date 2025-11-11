package icu.windea.pls.ep.match

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.data.complexEnums
import icu.windea.pls.config.data.enums
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher.*
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.match.ParadoxMatchProvider
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxType

class ParadoxBaseScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: Context): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.Any -> ParadoxMatchResult.FallbackMatch
            CwtDataTypes.Bool -> matchBool(context)
            CwtDataTypes.Int -> matchInt(context)
            CwtDataTypes.Float -> matchFloat(context)
            CwtDataTypes.Scalar -> matchScalar(context)
            CwtDataTypes.ColorField -> matchColorField(context)
            CwtDataTypes.Block -> matchBlock(context)
            else -> null
        }
    }

    private fun matchBool(context: Context): ParadoxMatchResult {
        val r = context.expression.type.isBooleanType()
        return ParadoxMatchResult.of(r)
    }

    private fun matchInt(context: Context): ParadoxMatchResult {
        // quoted number (e.g., "1") -> ok according to vanilla game files
        val value = context.expression.value
        val r = context.expression.type.isIntType() || ParadoxTypeResolver.resolve(value).isIntType()
        if (!r) return ParadoxMatchResult.NotMatch
        ParadoxMatchResultProvider.forRangedInt(value, context.configExpression)?.let { return it }
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchFloat(context: Context): ParadoxMatchResult {
        // quoted number (e.g., "1.0") -> ok according to vanilla game files
        val value = context.expression.value
        val r = context.expression.type.isFloatType() || ParadoxTypeResolver.resolve(value).isFloatType()
        if (!r) return ParadoxMatchResult.NotMatch
        ParadoxMatchResultProvider.forRangedFloat(value, context.configExpression)?.let { return it }
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchScalar(context: Context): ParadoxMatchResult {
        val r = when {
            context.expression.isKey == true -> true // key -> ok
            context.expression.type == ParadoxType.Parameter -> true // parameter -> ok
            context.expression.type.isBooleanType() -> true // boolean -> sadly, also ok for compatibility
            context.expression.type.isIntType() -> true // number -> ok according to vanilla game files
            context.expression.type.isFloatType() -> true // number -> ok according to vanilla game files
            context.expression.type.isStringType() -> true // unquoted/quoted string -> ok
            else -> false
        }
        return ParadoxMatchResult.ofFallback(r)
    }

    private fun matchColorField(context: Context): ParadoxMatchResult {
        val r = context.expression.type.isColorType() && context.configExpression.value?.let { context.expression.value.startsWith(it) } != false
        return ParadoxMatchResult.of(r)
    }

    private fun matchBlock(context: Context): ParadoxMatchResult {
        if (context.expression.isKey != false) return ParadoxMatchResult.NotMatch
        if (context.expression.type != ParadoxType.Block) return ParadoxMatchResult.NotMatch
        if (context.config !is CwtMemberConfig) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forBlock(context.element, context.config)
    }
}

class ParadoxCoreScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: Context): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.PercentageField -> matchPercentageField(context)
            CwtDataTypes.DateField -> matchDataField(context)
            CwtDataTypes.Definition, CwtDataTypes.SuffixAwareDefinition -> matchDefinition(context)
            CwtDataTypes.Localisation, CwtDataTypes.SuffixAwareLocalisation -> matchLocalisation(context)
            CwtDataTypes.SyncedLocalisation, CwtDataTypes.SuffixAwareSyncedLocalisation -> matchSyncedLocalisation(context)
            CwtDataTypes.InlineLocalisation -> matchInlineLocalisation(context)
            CwtDataTypes.AbsoluteFilePath -> matchAbsoluteFilePath(context)
            in CwtDataTypeGroups.PathReference -> matchPathReference(context)
            CwtDataTypes.EnumValue -> matchEnumValue(context)
            in CwtDataTypeGroups.DynamicValue -> matchDynamicValue(context)
            in CwtDataTypeGroups.ScopeField -> matchScopeFieldExpression(context)
            in CwtDataTypeGroups.ValueField -> matchValueFieldExpression(context)
            in CwtDataTypeGroups.VariableField -> matchVariableFieldExpression(context)
            CwtDataTypes.Modifier -> matchModifier(context)
            CwtDataTypes.SingleAliasRight -> ParadoxMatchResult.NotMatch // 不在这里处理
            CwtDataTypes.AliasKeysField -> matchAliasName(context)
            CwtDataTypes.AliasName -> matchAliasName(context)
            CwtDataTypes.AliasMatchLeft -> ParadoxMatchResult.NotMatch // 不在这里处理
            CwtDataTypes.Parameter -> matchParameter(context)
            CwtDataTypes.ParameterValue -> matchParameterValue(context)
            CwtDataTypes.LocalisationParameter -> matchLocalisationParameter(context)
            CwtDataTypes.DatabaseObject -> matchDatabaseObjectExpression(context)
            CwtDataTypes.DefineReference -> matchDefineReferenceExpression(context)
            CwtDataTypes.StellarisNameFormat -> matchStellarisNameFormatExpression(context)
            CwtDataTypes.ShaderEffect -> matchShaderEffect(context)
            CwtDataTypes.TechnologyWithLevel -> matchTechnologyWithLevel(context)
            else -> null
        }
    }

    private fun matchPercentageField(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        val r = ParadoxTypeResolver.isPercentageField(context.expression.value)
        return ParadoxMatchResult.of(r)
    }

    private fun matchDataField(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        val datePattern = context.configExpression.value
        val r = ParadoxTypeResolver.isDateField(context.expression.value, datePattern)
        return ParadoxMatchResult.of(r)
    }

    private fun matchDefinition(context: Context): ParadoxMatchResult {
        val expression = context.expression.value
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isStringType() && context.expression.type != ParadoxType.Int && context.expression.type != ParadoxType.Float) return ParadoxMatchResult.NotMatch
        if (!expression.isParameterAwareIdentifier('.', '-')) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDefinition(context.element, context.project, expression, context.configExpression)
    }

    private fun matchLocalisation(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forLocalisation(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchSyncedLocalisation(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forSyncedLocalisation(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchInlineLocalisation(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.quoted) return ParadoxMatchResult.FallbackMatch // "quoted_string" -> any string
        if (!context.expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forLocalisation(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchAbsoluteFilePath(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchPathReference(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forPathReference(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchEnumValue(context: Context): ParadoxMatchResult {
        if (context.expression.type.isBlockLikeType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val name = context.expression.value
        val enumName = context.configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        // match simple enums
        val enumConfig = context.configGroup.enums[enumName]
        if (enumConfig != null) {
            val r = name in enumConfig.values
            return ParadoxMatchResult.of(r)
        }
        // match complex enums
        val complexEnumConfig = context.configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            // complexEnumValue的值必须合法
            if (ParadoxComplexEnumValueManager.getName(context.expression.value) == null) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResultProvider.forComplexEnumValue(context.element, context.project, name, enumName, complexEnumConfig)
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: Context): ParadoxMatchResult {
        if (context.expression.type.isBlockLikeType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        // dynamicValue的值必须合法
        val name = ParadoxDynamicValueManager.getName(context.expression.value) ?: return ParadoxMatchResult.NotMatch
        if (!name.isIdentifier('.')) return ParadoxMatchResult.NotMatch
        val dynamicValueType = context.configExpression.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchScopeFieldExpression(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forScopeFieldExpression(context.configGroup, context.expression.value, context.configExpression, context.element)
    }

    private fun matchValueFieldExpression(context: Context): ParadoxMatchResult {
        // 也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
        val value = context.expression.value
        val type = context.expression.type
        if (context.dataType == CwtDataTypes.ValueField) {
            if (type.isFloatType() || ParadoxTypeResolver.resolve(value).isFloatType()) return ParadoxMatchResult.ExactMatch
        } else if (context.dataType == CwtDataTypes.IntValueField) {
            if (type.isIntType() || ParadoxTypeResolver.resolve(value).isIntType()) return ParadoxMatchResult.ExactMatch
        }
        if (!type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forValueFieldExpression(context.configGroup, value)
    }

    private fun matchVariableFieldExpression(context: Context): ParadoxMatchResult {
        // 也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
        val value = context.expression.value
        val type = context.expression.type
        if (context.dataType == CwtDataTypes.VariableField) {
            if (type.isFloatType() || ParadoxTypeResolver.resolve(value).isFloatType()) return ParadoxMatchResult.ExactMatch
        } else if (context.dataType == CwtDataTypes.IntVariableField) {
            if (type.isIntType() || ParadoxTypeResolver.resolve(value).isIntType()) return ParadoxMatchResult.ExactMatch
        }
        if (!type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forVariableFieldExpression(context.configGroup, value)
    }

    private fun matchModifier(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forModifier(context.element, context.configGroup, context.expression.value)
    }

    private fun matchAliasName(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val aliasName = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        val aliasSubName = ParadoxExpressionManager.getMatchedAliasKey(context.configGroup, aliasName, context.expression.value, context.element, context.expression.quoted, context.options) ?: return ParadoxMatchResult.NotMatch
        return ParadoxMatchService.matchScriptExpression(context.element, context.expression, CwtDataExpression.resolve(aliasSubName, true), null, context.configGroup, context.options)
    }

    private fun matchParameter(context: Context): ParadoxMatchResult {
        // 匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
        if (!context.expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier()) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchParameterValue(context: Context): ParadoxMatchResult {
        // 匹配参数值（只要不是子句即可匹配）
        if (context.expression.type == ParadoxType.Block) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchLocalisationParameter(context: Context): ParadoxMatchResult {
        // 匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
        if (!context.expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier('.', '-', '\'')) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchDatabaseObjectExpression(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDatabaseObjectExpression(context.configGroup, context.expression.value)
    }

    private fun matchDefineReferenceExpression(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDefineReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchStellarisNameFormatExpression(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if(context.config == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forStellarisNameFormatExpression(context.configGroup, context.expression.value, context.config)
    }

    private fun matchShaderEffect(context: Context): ParadoxMatchResult {
        // TODO 1.2.2+ 暂时作为一般的字符串处理
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchTechnologyWithLevel(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.value.length > 1 && context.expression.value.indexOf('@') >= 1) return ParadoxMatchResult.ExactMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }
}

class ParadoxConstantScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: Context): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.Constant) return null
        val value = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        if (!context.configExpression.isKey) {
            // 作为常量的值也可能是yes/no
            val text = context.expression.value
            if ((value == "yes" || value == "no") && text.isLeftQuoted()) return ParadoxMatchResult.NotMatch
        }
        // 这里也用来匹配空字符串
        val r = context.expression.matchesConstant(value)
        return ParadoxMatchResult.of(r)
    }
}

class ParadoxTemplateScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun isPatternAware() = true

    override fun match(context: Context): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.TemplateExpression) return null
        if (!context.expression.type.isStringLikeType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        // 允许用引号括起
        return ParadoxMatchResultProvider.forTemplate(context.element, context.configGroup, context.expression.value, context.configExpression)
    }
}

class ParadoxAntScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun isPatternAware() = true

    override fun match(context: Context): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.Ant) return null
        val pattern = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        val ignoreCase = context.configExpression.ignoreCase ?: false
        val r = context.expression.value.matchesAntPattern(pattern, ignoreCase)
        return ParadoxMatchResult.of(r)
    }
}

class ParadoxRegexScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun isPatternAware() = true

    override fun match(context: Context): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.Regex) return null
        val pattern = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        val ignoreCase = context.configExpression.ignoreCase ?: false
        val r = context.expression.value.matchesRegex(pattern, ignoreCase)
        return ParadoxMatchResult.of(r)
    }
}

class ParadoxPredicateBasedScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: Context): ParadoxMatchResult? {
        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        // 这里的 config 也可能是属性值对应的规则，因此下面需要传入 config.memberConfig
        val memberConfig = if (context.config is CwtMemberConfig<*>) context.config.memberConfig else null
        if (memberConfig == null) return null
        if (!ParadoxMatchProvider.matchesByPredicate(context.element, memberConfig)) return ParadoxMatchResult.NotMatch
        return null
    }
}
