package icu.windea.pls.ep.match.expression

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandUnionCandidates
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext
import icu.windea.pls.lang.match.util.ParadoxMatchProvider
import icu.windea.pls.lang.match.util.ParadoxMatchResultProvider
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxExpressionType

class ParadoxScriptBasicExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Any) { ParadoxMatchResult.FallbackMatch }
        register(CwtDataTypes.Bool) { matchBool(it) }
        register(CwtDataTypes.Int) { matchInt(it) }
        register(CwtDataTypes.Float) { matchFloat(it) }
        register(CwtDataTypes.Scalar) { matchScalar(it) }
        register(CwtDataTypes.ColorField) { matchColorField(it) }
        register(CwtDataTypes.Block) { matchBlock(it) }
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
        val r = context.expression.type == ParadoxExpressionType.Color && context.configExpression.metadata.value?.let { context.expression.value.startsWith(it) } != false
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchBlock(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.role != ParadoxExpressionRole.Value) return ParadoxMatchResult.NotMatch
        if (context.expression.type != ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
        if (context.config !is CwtMemberConfig) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forBlock(context.element, context.config)
    }
}

class ParadoxScriptExtraBasicExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.PercentageField) { matchPercentageField(it) }
        register(CwtDataTypes.IntPercentageField) { matchIntPercentageField(it) }
        register(CwtDataTypes.DateField) { matchDataField(it) }
    }

    private fun matchPercentageField(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = ParadoxMatchProvider.matchesFloatPercentageField(context.expression.value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchIntPercentageField(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = ParadoxMatchProvider.matchesIntPercentageField(context.expression.value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchDataField(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val datePattern = context.configExpression.metadata.value
        val r = ParadoxMatchProvider.matchesDateField(context.expression.value, datePattern)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxScriptCoreExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Definition, CwtDataTypes.SuffixAwareDefinition) { matchDefinition(it) }
        register(CwtDataTypes.Localisation, CwtDataTypes.SuffixAwareLocalisation) { matchLocalisation(it) }
        register(CwtDataTypes.SyncedLocalisation, CwtDataTypes.SuffixAwareSyncedLocalisation) { matchSyncedLocalisation(it) }
        register(CwtDataTypes.InlineLocalisation) { matchInlineLocalisation(it) }
        register(CwtDataTypeSets.PathReference) { matchPathReference(it) }
        register(CwtDataTypes.EnumValue) { matchEnumValue(it) }
        register(CwtDataTypes.UnionValue) { matchUnionValue(it) }
        register(CwtDataTypeSets.DynamicValue) { matchDynamicValue(it) }
        register(CwtDataTypeSets.ScopeField) { matchScopeFieldExpression(it) }
        register(CwtDataTypeSets.ValueField) { matchValueFieldExpression(it) }
        register(CwtDataTypeSets.VariableField) { matchVariableFieldExpression(it) }
        register(CwtDataTypes.Modifier) { matchModifier(it) }
        register(CwtDataTypes.AliasKeysField) { matchAliasName(it) }
        register(CwtDataTypes.AliasName) { matchAliasName(it) }
        register(CwtDataTypes.AliasMatchLeft) { ParadoxMatchResult.NotMatch } // 不在这里处理
        register(CwtDataTypes.SingleAliasRight) { ParadoxMatchResult.NotMatch } // 不在这里处理
        register(CwtDataTypes.Command) { ParadoxMatchResult.NotMatch } // TODO 2.1.1+ 目前不支持用来匹配脚本表达式
        register(CwtDataTypes.Template) { matchTemplateExpression(it) }
        register(CwtDataTypes.ScriptValueReference) { matchScriptValueReferenceExpression(it) }
        register(CwtDataTypes.DefineReference) { matchDefineReferenceExpression(it) }
        register(CwtDataTypes.ArrayDefineReference) { matchArrayDefineReferenceExpression(it) }
        register(CwtDataTypes.Tags) { matchTagsExpression(it) }
        register(CwtDataTypes.DatabaseObject) { matchDatabaseObjectExpression(it) }
        register(CwtDataTypes.NameFormat) { matchNameFormatExpression(it) }
        register(CwtDataTypes.Parameter) { matchParameter(it) }
        register(CwtDataTypes.ParameterValue) { matchParameterValue(it) }
        register(CwtDataTypes.LocalisationParameter) { matchLocalisationParameter(it) }
        register(CwtDataTypes.ShaderEffect) { matchShaderEffect(it) }
        register(CwtDataTypes.MeshLocator) { matchMeshLocator(it) }
        register(CwtDataTypes.TechnologyWithLevel) { matchTechnologyWithLevel(it) }
    }

    private fun matchDefinition(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        // if (!context.expression.value.isParameterAwareIdentifier(".-")) return ParadoxMatchResult.NotMatch // #369 can also be any string literals
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
        val enumName = context.configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
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

    private fun matchUnionValue(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        val unionName = context.configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        val unionConfig = context.configGroup.unions[unionName] ?: return ParadoxMatchResult.NotMatch // null -> not match
        // NOTE 3.0.1 recursion guard is required here
        return ProcessorScope.findFrom {
            runWithRecursionGuard("scriptExpression.match.union", unionName) {
                unionConfig.expandUnionCandidates { valueConfig ->
                    ProgressManager.checkCanceled() // check cancellation
                    val nextContext = context.copy(configExpression = valueConfig.configExpression)
                    val r = ParadoxExpressionMatchService.matchScriptExpression(nextContext)
                    if (r.get(context.options)) process(r)
                    else true
                }
            }
        } ?: ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.type.isBlockLike()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        val name = context.expression.value.substringBefore('@')
        if (!name.isParameterAwareIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = context.configExpression.metadata.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchScopeFieldExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forScopeFieldExpression(context.element, context.configGroup, context.expression.value, context.configExpression)
    }

    private fun matchValueFieldExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 兼容数字字面量（包括用引号括起的数字字面量）
        val dataType = context.dataType
        if (dataType == CwtDataTypes.ValueField) {
            if (context.expression.matchesFloat()) return ParadoxMatchResult.ExactMatch
        } else if (dataType == CwtDataTypes.IntValueField) {
            if (context.expression.matchesInt()) return ParadoxMatchResult.ExactMatch
        }
        val text = context.expression.value
        val type = context.expression.type
        if (!type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forValueFieldExpression(context.configGroup, text)
    }

    private fun matchVariableFieldExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // 兼容数字字面量（包括用引号括起的数字字面量）
        val dataType = context.dataType
        if (dataType == CwtDataTypes.VariableField) {
            if (context.expression.matchesFloat()) return ParadoxMatchResult.ExactMatch
        } else if (dataType == CwtDataTypes.IntVariableField) {
            if (context.expression.matchesInt()) return ParadoxMatchResult.ExactMatch
        }
        val text = context.expression.value
        val type = context.expression.type
        if (!type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forVariableFieldExpression(context.configGroup, text)
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
        val aliasName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch
        val aliasExpression = expression
        val aliasSubName = ParadoxExpressionMatchService.getMatchedAliasKey(element, aliasExpression, aliasName, configGroup, options) ?: return ParadoxMatchResult.NotMatch
        val nextContext = ParadoxScriptExpressionMatchContext(element, expression, CwtDataExpression.resolve(aliasSubName), null, configGroup, options)
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

    private fun matchScriptValueReferenceExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forScriptValueReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchDefineReferenceExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDefineReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchArrayDefineReferenceExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forArrayDefineReferenceExpression(context.configGroup, context.expression.value)
    }

    private fun matchTagsExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.FallbackMatch // 2.1.10 compatible
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (context.config == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forTagsExpression(context.configGroup, context.expression.value, context.config)
    }

    private fun matchDatabaseObjectExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forDatabaseObjectExpression(context.configGroup, context.expression.value)
    }

    private fun matchNameFormatExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        if (context.config == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forNameFormatExpression(context.configGroup, context.expression.value, context.config)
    }

    private fun matchTemplateExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultProvider.forTemplate(context.element, context.configGroup, context.expression.value, context.configExpression, context.options)
    }

    private fun matchShaderEffect(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchMeshLocator(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.FallbackMatch // NOTE 2.1.9 empty string is specially allowed
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchTechnologyWithLevel(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        if (context.expression.value.length > 1 && context.expression.value.indexOf('@') >= 1) return ParadoxMatchResult.WildcardMatch
        return ParadoxMatchResult.NotMatch
    }
}

class ParadoxScriptConstantExpressionMatcher : ParadoxScriptCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Constant) { matchConstant(it) }
    }

    private fun matchConstant(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        val expression = context.expression
        val configExpression = context.configExpression
        // 兼容空字符串，兼容带参数的情况
        val r = expression.matchesConstant(configExpression.expressionString)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxScriptPatternExpressionMatcher : ParadoxScriptSimpleExpressionMatcher() {
    override val dataTypes: Array<CwtDataType> = CwtDataTypeSets.Pattern

    override fun isPatternAware(context: ParadoxScriptExpressionMatchContext) = true

    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        val pattern = context.configExpression.metadata.value ?: return null
        val ignoreCase = context.configExpression.metadata.ignoreCase
        val text = context.expression.value
        val r = when (context.dataType) {
            CwtDataTypes.Glob -> text.matchesPattern(pattern, ignoreCase)
            CwtDataTypes.Ant -> text.matchesAntPattern(pattern, ignoreCase)
            CwtDataTypes.Regex -> text.matchesRegex(pattern, ignoreCase)
            else -> null
        }
        if (r == null) return null
        return ParadoxMatchResult.exactOrNot(r)
    }
}

// NOTE 3.0.1 目前从未被实际使用
class ParadoxScriptPredicateBasedExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        // 3.0.1 optimize: use attribute to apply fast return
        if (!context.usePredicateBasedMatch) return null

        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        val config = context.config
        if (config !is CwtMemberConfig<*>) return null
        if (!ParadoxMatchProvider.matchesByPredicate(context.element, config)) return ParadoxMatchResult.NotMatch
        return null
    }
}
