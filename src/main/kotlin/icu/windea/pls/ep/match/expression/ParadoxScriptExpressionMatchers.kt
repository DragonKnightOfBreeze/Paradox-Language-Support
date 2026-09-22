package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.isIdentifier
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchFactory
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxExpressionType

@Suppress("UNUSED_PARAMETER")
class ParadoxScriptBasicExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Any, ::matchAny)
        register(CwtDataTypes.Bool, ::matchBool)
        register(CwtDataTypes.Int, ::matchInt)
        register(CwtDataTypes.Float, ::matchFloat)
        register(CwtDataTypes.Scalar, ::matchScalar)
        register(CwtDataTypes.ColorField, ::matchColorField)
        register(CwtDataTypes.Block, ::matchBlock)
    }

    private fun matchAny(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchBool(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (context.expression.type.isLenientBooleanLiteral()) {
            return ParadoxMatchResult.ExactMatch
        }
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchInt(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // quoted number (e.g., `"1"`) -> ok according to vanilla game files
        if (context.expression.matchesInt()) {
            ParadoxMatchResultFactory.forRangedInt(context.expression, configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchFloat(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // quoted number (e.g., `"1.0"`) -> ok according to vanilla game files
        if (context.expression.matchesFloat()) {
            ParadoxMatchResultFactory.forRangedFloat(context.expression, configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchScalar(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        val r = when {
            context.expression.role == ParadoxExpressionRole.Key -> true // key -> ok
            context.expression.type.isLenientBooleanLiteral() -> true // boolean -> sadly, also ok for compatibility
            context.expression.type.isLenientNumberLiteral() -> true // number -> ok according to vanilla game files
            context.expression.type.isLenientStringLiteral() -> true // unquoted/quoted string -> ok
            else -> false
        }
        if (r) return ParadoxMatchResult.FallbackMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchColorField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        val r = context.expression.type == ParadoxExpressionType.Color && configExpression.metadata.value?.let { context.expression.value.startsWith(it) } != false
        if (r) return ParadoxMatchResult.ExactMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchBlock(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (config !is CwtMemberConfig) return ParadoxMatchResult.NotMatch
        if (context.expression.role != ParadoxExpressionRole.Value) return ParadoxMatchResult.NotMatch
        if (context.expression.type != ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch // also possible
        return ParadoxMatchResultFactory.forBlock(context.element, config)
    }
}

@Suppress("UNUSED_PARAMETER")
class ParadoxScriptExtraBasicExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.PercentageField, ::matchPercentageField)
        register(CwtDataTypes.IntPercentageField, ::matchIntPercentageField)
        register(CwtDataTypes.DateField, ::matchDataField)
    }

    private fun matchPercentageField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val r = ParadoxMatchFactory.matchesFloatPercentageField(context.expression.value)
        if (r) return ParadoxMatchResult.ExactMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchIntPercentageField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val r = ParadoxMatchFactory.matchesIntPercentageField(context.expression.value)
        if (r) return ParadoxMatchResult.ExactMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDataField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val datePattern = configExpression.metadata.value
        val r = ParadoxMatchFactory.matchesDateField(context.expression.value, datePattern)
        if (r) return ParadoxMatchResult.ExactMatch
        if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.NotMatch
    }
}

@Suppress("UNUSED_PARAMETER")
class ParadoxScriptCoreExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Definition, ::matchDefinition)
        register(CwtDataTypes.SuffixAwareDefinition, ::matchDefinition)
        register(CwtDataTypes.Localisation, ::matchLocalisation)
        register(CwtDataTypes.SuffixAwareLocalisation, ::matchLocalisation)
        register(CwtDataTypes.SyncedLocalisation, ::matchSyncedLocalisation)
        register(CwtDataTypes.SuffixAwareSyncedLocalisation, ::matchSyncedLocalisation)
        register(CwtDataTypes.InlineLocalisation, ::matchInlineLocalisation)
        register(CwtDataTypeSets.PathReference, ::matchPathReference)
        register(CwtDataTypes.EnumValue, ::matchEnumValue)
        register(CwtDataTypes.UnionValue, ::matchUnionValue)
        register(CwtDataTypeSets.DynamicValue, ::matchDynamicValue)
        register(CwtDataTypeSets.ScopeField, ::matchScopeFieldExpression)
        register(CwtDataTypeSets.ValueField, ::matchValueFieldExpression)
        register(CwtDataTypeSets.VariableField, ::matchVariableFieldExpression)
        register(CwtDataTypes.Modifier, ::matchModifier)
        register(CwtDataTypes.AliasKeysField, ::matchAliasName)
        register(CwtDataTypes.AliasName, ::matchAliasName)
        register(CwtDataTypes.AliasMatchLeft) { _, _, _ -> ParadoxMatchResult.NotMatch } // 不在这里处理
        register(CwtDataTypes.SingleAliasRight) { _, _, _ -> ParadoxMatchResult.NotMatch } // 不在这里处理
        register(CwtDataTypes.Command) { _, _, _ -> ParadoxMatchResult.NotMatch } // TODO 2.1.1+ 目前不支持用来匹配脚本表达式
        register(CwtDataTypes.Template, ::matchTemplateExpression)
        register(CwtDataTypes.ScriptValueReference, ::matchScriptValueReferenceExpression)
        register(CwtDataTypes.DefineReference, ::matchDefineReferenceExpression)
        register(CwtDataTypes.ArrayDefineReference, ::matchArrayDefineReferenceExpression)
        register(CwtDataTypes.Tags, ::matchTagsExpression)
        register(CwtDataTypes.DatabaseObject, ::matchDatabaseObjectExpression)
        register(CwtDataTypes.NameFormat, ::matchNameFormatExpression)
        register(CwtDataTypes.Parameter, ::matchParameter)
        register(CwtDataTypes.ParameterValue, ::matchParameterValue)
        register(CwtDataTypes.LocalisationParameter, ::matchLocalisationParameter)
        register(CwtDataTypes.ShaderEffect, ::matchShaderEffect)
        register(CwtDataTypes.MeshLocator, ::matchMeshLocator)
        register(CwtDataTypes.TechnologyWithLevel, ::matchTechnologyWithLevel)
    }

    private fun matchDefinition(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        // if (!context.expression.value.isIdentifier(".-")) return ParadoxMatchResult.NotMatch // #369 can also be any string literals
        return ParadoxMatchResultFactory.forDefinition(context.element, context.project, context.expression.value, configExpression)
    }

    private fun matchLocalisation(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultFactory.forLocalisation(context.element, context.project, context.expression.value, configExpression)
    }

    private fun matchSyncedLocalisation(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forSyncedLocalisation(context.element, context.project, context.expression.value, configExpression)
    }

    private fun matchInlineLocalisation(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.quoted) return ParadoxMatchResult.FallbackMatch // "quoted_string" -> any string
        if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forLocalisation(context.element, context.project, context.expression.value, configExpression)
    }

    private fun matchPathReference(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forPathReference(context.element, context.project, context.expression.value, configExpression)
    }

    private fun matchEnumValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val name = context.expression.value
        val enumName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        // match simple enums
        val enumConfig = context.configGroup.enums[enumName]
        if (enumConfig != null) {
            val r = name in enumConfig.values
            return ParadoxMatchResult.exactOrNot(r)
        }
        // match complex enums
        val complexEnumConfig = context.configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            return ParadoxMatchResultFactory.forComplexEnumValue(context.element, context.project, name, enumName, complexEnumConfig)
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val name = context.expression.value.substringBefore('@')
        if (!name.isIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = configExpression.metadata.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchScopeFieldExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forScopeFieldExpression(context.element, context.configGroup, context.expression.value, configExpression)
    }

    private fun matchValueFieldExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 兼容数字字面量（包括用引号括起的数字字面量）
        val dataType = configExpression.type
        if (dataType == CwtDataTypes.ValueField) {
            if (context.expression.matchesFloat()) return ParadoxMatchResult.ExactMatch
        } else if (dataType == CwtDataTypes.IntValueField) {
            if (context.expression.matchesInt()) return ParadoxMatchResult.ExactMatch
        }
        val text = context.expression.value
        val type = context.expression.type
        if (!type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forValueFieldExpression(context.configGroup, text)
    }

    private fun matchVariableFieldExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 兼容数字字面量（包括用引号括起的数字字面量）
        val dataType = configExpression.type
        if (dataType == CwtDataTypes.VariableField) {
            if (context.expression.matchesFloat()) return ParadoxMatchResult.ExactMatch
        } else if (dataType == CwtDataTypes.IntVariableField) {
            if (context.expression.matchesInt()) return ParadoxMatchResult.ExactMatch
        }
        val text = context.expression.value
        val type = context.expression.type
        if (!type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forVariableFieldExpression(context.configGroup, text)
    }

    private fun matchModifier(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (!context.expression.value.isIdentifier()) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultFactory.forModifier(context.element, context.configGroup, context.expression.value)
    }

    private fun matchUnionValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch // 3.0.2 fast return
        val unionName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        // NOTE 3.0.1 recursion guard is required here
        val processor = ProcessorFactory.find<ParadoxMatchResult>()
        runWithRecursionGuard("scriptExpression.match.union", unionName) {
            ParadoxConfigExpansionService.expandAndMatchUnion(context.element, context.expression, unionName, context.configGroup, context.options) { _, matchResult ->
                processor.process(matchResult)
            }
        }
        return processor.result ?: ParadoxMatchResult.NotMatch
    }

    private fun matchAliasName(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val aliasName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch
        // NOTE 3.0.1 recursion guard is required here
        val processor = ProcessorFactory.find<ParadoxMatchResult>()
        runWithRecursionGuard("scriptExpression.match.alias", aliasName) {
            ParadoxConfigExpansionService.expandAndMatchAliasKeys(context.element, context.expression, aliasName, context.configGroup, context.options) { _, r ->
                processor.process(r)
            }
        }
        return processor.result ?: ParadoxMatchResult.NotMatch
    }

    private fun matchParameter(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
        // 3.0.3 必须形如标识符（不允许带参数）
        if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.NotMatch
        if (!context.expression.value.isIdentifier()) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchParameterValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 匹配参数值（只要不是子句即可匹配）
        if (context.expression.type == ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchLocalisationParameter(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
        // 3.0.3 必须形如标识符（可以带参数）
        if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchScriptValueReferenceExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forScriptValueReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchDefineReferenceExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forDefineReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchArrayDefineReferenceExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forArrayDefineReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchTagsExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (config == null) return ParadoxMatchResult.NotMatch
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.FallbackMatch // 2.1.10 compatible
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forTagsExpression(context.configGroup, context.expression.value, config)
    }

    private fun matchDatabaseObjectExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forDatabaseObjectExpression(context.configGroup, context.expression.value)
    }

    private fun matchNameFormatExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (config == null) return ParadoxMatchResult.NotMatch
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forNameFormatExpression(context.configGroup, context.expression.value, config)
    }

    private fun matchTemplateExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forTemplate(context.element, context.configGroup, context.expression.value, configExpression, context.options)
    }

    private fun matchShaderEffect(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchMeshLocator(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.FallbackMatch // NOTE 2.1.9 empty string is specially allowed
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchTechnologyWithLevel(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (context.expression.value.length > 1 && context.expression.value.indexOf('@') >= 1) return ParadoxMatchResult.WildcardMatch
        return ParadoxMatchResult.NotMatch
    }
}

class ParadoxScriptConstantExpressionMatcher : ParadoxScriptSimpleExpressionMatcher() {
    override val dataTypes = CwtDataTypeSets.Constant

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 兼容空字符串，兼容带参数的情况
        if (context.expression.isFullParameterized()) {
            return ParadoxMatchResult.ParameterizedMatch
        }
        if (context.expression.isParameterized()) {
            if (context.expression.matchesRegex(configExpression.expressionString)) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
        val r = context.expression.matchesConstant(configExpression.expressionString)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxScriptPatternExpressionMatcher : ParadoxScriptSimpleExpressionMatcher() {
    override val dataTypes = CwtDataTypeSets.Pattern

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult? {
        val pattern = configExpression.metadata.value ?: return null
        val ignoreCase = configExpression.metadata.ignoreCase
        val text = context.expression.value
        val r = when (configExpression.type) {
            CwtDataTypes.Glob -> text.matchesPattern(pattern, ignoreCase)
            CwtDataTypes.Ant -> text.matchesAntPattern(pattern, ignoreCase)
            CwtDataTypes.Regex -> text.matchesRegex(pattern, ignoreCase)
            else -> return null
        }
        return ParadoxMatchResult.exactOrNot(r)
    }
}

// NOTE 3.0.1 目前从未被实际使用
class ParadoxScriptPredicateBasedExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult? {
        // 3.0.1 optimize: use attribute to apply fast return
        if (!context.usePredicateBasedMatch) return null

        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        if (config !is CwtMemberConfig<*>) return null
        if (!ParadoxMatchFactory.matchesByPredicate(context.element, config)) return ParadoxMatchResult.NotMatch
        return null
    }
}
