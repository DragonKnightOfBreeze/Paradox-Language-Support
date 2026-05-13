package icu.windea.pls.ep.match

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.core.text.TextMatcher
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchProvider
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxExpressionType

class ParadoxBaseScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
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

    private fun matchBool(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        val r = context.expression.type == ParadoxExpressionType.Boolean
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchInt(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // quoted number (e.g., "1") -> ok according to vanilla game files
        if (context.expression.matchesInt()) {
            ParadoxMatchResultProvider.forRangedInt(context.expression, context.configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchFloat(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // quoted number (e.g., "1.0") -> ok according to vanilla game files
        if (context.expression.matchesFloat()) {
            ParadoxMatchResultProvider.forRangedFloat(context.expression, context.configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchScalar(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        val r = when {
            context.expression.role == ParadoxExpressionRole.Key -> true // key -> ok
            context.expression.type == ParadoxExpressionType.Boolean -> true // boolean -> sadly, also ok for compatibility
            context.expression.type.isLenientFloat() -> true // number -> ok according to vanilla game files
            context.expression.type.isLenientString() -> true // unquoted/quoted string -> ok
            else -> false
        }
        return ParadoxMatchResult.fallbackOrNot(r)
    }

    private fun matchColorField(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        val r = context.expression.type == ParadoxExpressionType.Color && context.configExpression.value?.let { context.expression.value.startsWith(it) } != false
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchBlock(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.role != ParadoxExpressionRole.Value) return ParadoxMatchResult.NotMatch
        if (context.expression.type != ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
        if (context.config !is CwtMemberConfig) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forBlock(context.element, context.config)
    }
}

class ParadoxCoreScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.PercentageField -> matchPercentageField(context)
            CwtDataTypes.DateField -> matchDataField(context)
            CwtDataTypes.Definition, CwtDataTypes.SuffixAwareDefinition -> matchDefinition(context)
            CwtDataTypes.Localisation, CwtDataTypes.SuffixAwareLocalisation -> matchLocalisation(context)
            CwtDataTypes.SyncedLocalisation, CwtDataTypes.SuffixAwareSyncedLocalisation -> matchSyncedLocalisation(context)
            CwtDataTypes.InlineLocalisation -> matchInlineLocalisation(context)
            in CwtDataTypeSets.PathReference -> matchPathReference(context)
            CwtDataTypes.EnumValue -> matchEnumValue(context)
            in CwtDataTypeSets.DynamicValue -> matchDynamicValue(context)
            in CwtDataTypeSets.ScopeField -> matchScopeFieldExpression(context)
            in CwtDataTypeSets.ValueField -> matchValueFieldExpression(context)
            in CwtDataTypeSets.VariableField -> matchVariableFieldExpression(context)
            CwtDataTypes.Modifier -> matchModifier(context)
            CwtDataTypes.SingleAliasRight -> ParadoxMatchResult.NotMatch // 不在这里处理
            CwtDataTypes.AliasKeysField -> matchAliasName(context)
            CwtDataTypes.AliasName -> matchAliasName(context)
            CwtDataTypes.AliasMatchLeft -> ParadoxMatchResult.NotMatch // 不在这里处理
            CwtDataTypes.Command -> null // TODO 2.1.1+ 目前不支持用来匹配脚本表达式
            CwtDataTypes.DefineReference -> matchDefineReferenceExpression(context)
            CwtDataTypes.DatabaseObject -> matchDatabaseObjectExpression(context)
            CwtDataTypes.NameFormat -> matchNameFormatExpression(context)
            CwtDataTypes.Parameter -> matchParameter(context)
            CwtDataTypes.ParameterValue -> matchParameterValue(context)
            CwtDataTypes.LocalisationParameter -> matchLocalisationParameter(context)
            CwtDataTypes.ShaderEffect -> matchShaderEffect(context)
            CwtDataTypes.TechnologyWithLevel -> matchTechnologyWithLevel(context)
            else -> null
        }
    }

    private fun matchPercentageField(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = TextMatcher.matchesPercentageField(context.expression.value, leadingUnary = false)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchDataField(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val datePattern = context.configExpression.value
        val r = TextMatcher.matchesDateField(context.expression.value, datePattern)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchDefinition(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier(".-")) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDefinition(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchLocalisation(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forLocalisation(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchSyncedLocalisation(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forSyncedLocalisation(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchInlineLocalisation(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.quoted) return ParadoxMatchResult.FallbackMatch // "quoted_string" -> any string
        if (!context.expression.value.isParameterAwareIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forLocalisation(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchPathReference(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forPathReference(context.element, context.project, context.expression.value, context.configExpression)
    }

    private fun matchEnumValue(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.type.isBlockLike()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val name = context.expression.value
        val enumName = context.configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        // match simple enums
        val enumConfig = context.configGroup.enums[enumName]
        if (enumConfig != null) {
            val r = name in enumConfig.values
            return ParadoxMatchResult.exactOrNot(r)
        }
        // match complex enums
        val complexEnumConfig = context.configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            return ParadoxMatchResultProvider.forComplexEnumValue(context.element, context.project, name, enumName, complexEnumConfig)
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.type.isBlockLike()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val name = context.expression.value.substringBefore('@')
        if (!name.isParameterAwareIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = context.configExpression.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchScopeFieldExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forScopeFieldExpression(context.configGroup, context.expression.value, context.configExpression, context.element)
    }

    private fun matchValueFieldExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 兼容数字字面量（包括用引号括起的数字字面量）
        val value = context.expression.value
        val type = context.expression.type
        if (context.dataType == CwtDataTypes.ValueField) {
            if (context.expression.matchesFloat()) return ParadoxMatchResult.ExactMatch
        } else if (context.dataType == CwtDataTypes.IntValueField) {
            if (context.expression.matchesInt()) return ParadoxMatchResult.ExactMatch
        }
        if (!type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forValueFieldExpression(context.configGroup, value)
    }

    private fun matchVariableFieldExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 兼容数字字面量（包括用引号括起的数字字面量）
        val value = context.expression.value
        val type = context.expression.type
        if (context.dataType == CwtDataTypes.VariableField) {
            if (context.expression.matchesFloat()) return ParadoxMatchResult.ExactMatch
        } else if (context.dataType == CwtDataTypes.IntVariableField) {
            if (context.expression.matchesInt()) return ParadoxMatchResult.ExactMatch
        }
        if (!type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forVariableFieldExpression(context.configGroup, value)
    }

    private fun matchModifier(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forModifier(context.element, context.configGroup, context.expression.value)
    }

    private fun matchAliasName(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val (element, expression, configExpression, _, configGroup, options) = context
        val aliasName = configExpression.value ?: return ParadoxMatchResult.NotMatch
        val aliasExpression = expression
        val aliasSubName = ParadoxExpressionMatchService.getMatchedAliasKey(element, aliasExpression, aliasName, configGroup, options) ?: return ParadoxMatchResult.NotMatch
        val nextContext = ParadoxScriptExpressionMatchContext(element, expression, CwtDataExpression.resolve(aliasSubName, true), null, configGroup, options)
        return ParadoxExpressionMatchService.matchScriptExpression(nextContext)
    }

    private fun matchParameter(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier()) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchParameterValue(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 匹配参数值（只要不是子句即可匹配）
        if (context.expression.type == ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchLocalisationParameter(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchDatabaseObjectExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDatabaseObjectExpression(context.configGroup, context.expression.value)
    }

    private fun matchDefineReferenceExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDefineReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchNameFormatExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (context.config == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forNameFormatExpression(context.configGroup, context.expression.value, context.config)
    }

    private fun matchShaderEffect(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // TODO 1.2.2+ 暂时作为一般的字符串处理
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchTechnologyWithLevel(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.value.length > 1 && context.expression.value.indexOf('@') >= 1) return ParadoxMatchResult.WildcardMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }
}

class ParadoxConstantScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.Constant) return null
        val value = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        if (!context.configExpression.isKey) {
            // 作为常量的值也可能是布尔值（`yes` / `no`）
            val text = context.expression.value
            if ((value == "yes" || value == "no") && text.isLeftQuoted()) return ParadoxMatchResult.NotMatch
        }
        // 兼容空字符串，兼容带参数的情况
        val r = context.expression.matchesConstant(value)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxTemplateScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun isPatternAware(context: ParadoxScriptExpressionMatchContext) = true

    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.TemplateExpression) return null
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        // 允许用引号括起
        return ParadoxMatchResultProvider.forTemplate(context.element, context.configGroup, context.expression.value, context.configExpression, context.options)
    }
}

class ParadoxAntScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun isPatternAware(context: ParadoxScriptExpressionMatchContext) = true

    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.Ant) return null
        val pattern = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        val ignoreCase = context.configExpression.ignoreCase
        val r = context.expression.value.matchesAntPattern(pattern, ignoreCase)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxRegexScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun isPatternAware(context: ParadoxScriptExpressionMatchContext) = true

    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        if (context.dataType != CwtDataTypes.Regex) return null
        val pattern = context.configExpression.value ?: return ParadoxMatchResult.NotMatch
        val ignoreCase = context.configExpression.ignoreCase
        val r = context.expression.value.matchesRegex(pattern, ignoreCase)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxPredicateBasedScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        val config = context.config
        if (config !is CwtMemberConfig<*>) return null
        if (!ParadoxMatchProvider.matchesByPredicate(context.element, config)) return ParadoxMatchResult.NotMatch
        return null
    }
}
