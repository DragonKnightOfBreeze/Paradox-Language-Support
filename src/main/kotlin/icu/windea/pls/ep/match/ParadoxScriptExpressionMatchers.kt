package icu.windea.pls.ep.match

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.enums
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
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
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

        val blockElement = when (context.element) {
            is ParadoxScriptProperty -> context.element.propertyValue()
            is ParadoxScriptBlock -> context.element
            else -> null
        } ?: return ParadoxMatchResult.NotMatch

        // 如果存在子句规则内容为空，则仅当子句内容为空时才认为匹配
        if (context.config.configs.isNullOrEmpty()) {
            if (blockElement.members().none()) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.FallbackMatch
        }

        return ParadoxMatchResult.LazyBlockAwareMatch p@{
            val keys = ParadoxExpressionManager.getInBlockKeys(context.config)
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
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isStringType() && context.expression.type != ParadoxType.Int && context.expression.type != ParadoxType.Float) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isParameterAwareIdentifier('.', '-')) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDefinition(context.element, context.project, context.expression.value, context.configExpression)
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
        val textRange = TextRange.create(0, context.expression.value.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(context.expression.value, textRange, context.configGroup)
        if (scopeFieldExpression == null) return ParadoxMatchResult.NotMatch
        if (scopeFieldExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResultProvider.forScopeField(context.element, context.configGroup, scopeFieldExpression, context.configExpression)
    }

    private fun matchValueFieldExpression(context: Context): ParadoxMatchResult {
        // 也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
        if (context.dataType == CwtDataTypes.ValueField) {
            if (context.expression.type.isFloatType() || ParadoxTypeResolver.resolve(context.expression.value).isFloatType()) return ParadoxMatchResult.ExactMatch
        } else if (context.dataType == CwtDataTypes.IntValueField) {
            if (context.expression.type.isIntType() || ParadoxTypeResolver.resolve(context.expression.value).isIntType()) return ParadoxMatchResult.ExactMatch
        }

        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val textRange = TextRange.create(0, context.expression.value.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(context.expression.value, textRange, context.configGroup)
        if (valueFieldExpression == null) return ParadoxMatchResult.NotMatch
        if (valueFieldExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchVariableFieldExpression(context: Context): ParadoxMatchResult {
        // 也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
        if (context.dataType == CwtDataTypes.VariableField) {
            if (context.expression.type.isFloatType() || ParadoxTypeResolver.resolve(context.expression.value).isFloatType()) return ParadoxMatchResult.ExactMatch
        } else if (context.dataType == CwtDataTypes.IntVariableField) {
            if (context.expression.type.isIntType() || ParadoxTypeResolver.resolve(context.expression.value).isIntType()) return ParadoxMatchResult.ExactMatch
        }

        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val textRange = TextRange.create(0, context.expression.value.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(context.expression.value, textRange, context.configGroup)
        if (variableFieldExpression == null) return ParadoxMatchResult.NotMatch
        if (variableFieldExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
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
        val textRange = TextRange.create(0, context.expression.value.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(context.expression.value, textRange, context.configGroup)
        if (databaseObjectExpression == null) return ParadoxMatchResult.NotMatch
        if (databaseObjectExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchDefineReferenceExpression(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val textRange = TextRange.create(0, context.expression.value.length)
        val defineReferenceExpression = ParadoxDefineReferenceExpression.resolve(context.expression.value, textRange, context.configGroup)
        if (defineReferenceExpression == null) return ParadoxMatchResult.NotMatch
        if (defineReferenceExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchStellarisNameFormatExpression(context: Context): ParadoxMatchResult {
        if (!context.expression.type.isStringType()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val textRange = TextRange.create(0, context.expression.value.length)
        val stellarisNameFormatExpression = StellarisNameFormatExpression.resolve(context.expression.value, textRange, context.configGroup, context.config ?: return ParadoxMatchResult.NotMatch)
        if (stellarisNameFormatExpression == null) return ParadoxMatchResult.NotMatch
        if (stellarisNameFormatExpression.getAllErrors(null).isNotEmpty()) return ParadoxMatchResult.PartialMatch
        return ParadoxMatchResult.ExactMatch
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
