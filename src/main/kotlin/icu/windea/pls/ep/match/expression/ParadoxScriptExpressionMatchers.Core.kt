package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.isIdentifier
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory
import icu.windea.pls.model.type.ParadoxExpressionType

abstract class ParadoxCoreScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    /** @see CwtDataTypes.Definition */
    class ForDefinition : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Definition || dataType == CwtDataTypes.SuffixAwareDefinition

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // can be an int or float here (e.g., for <technology_tier>)
            if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            // if (!context.expression.value.isIdentifier(".-")) return ParadoxMatchResult.NotMatch // #369 can also be any string literals
            return ParadoxMatchResultFactory.forDefinition(context.element, context.project, context.expression.value, configExpression)
        }
    }

    /** @see CwtDataTypes.Localisation */
    class ForLocalisation : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Localisation || dataType == CwtDataTypes.SuffixAwareLocalisation

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResultFactory.forLocalisation(context.element, context.project, context.expression.value, configExpression)
        }
    }

    /** @see CwtDataTypes.SyncedLocalisation */
    class ForSyncedLocalisation : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.SyncedLocalisation || dataType == CwtDataTypes.SuffixAwareSyncedLocalisation

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forSyncedLocalisation(context.element, context.project, context.expression.value, configExpression)
        }
    }

    /** @see CwtDataTypes.InlineLocalisation */
    class ForInlineLocalisation : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.InlineLocalisation

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.quoted) return ParadoxMatchResult.FallbackMatch // "quoted_string" -> any string
            if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forLocalisation(context.element, context.project, context.expression.value, configExpression)
        }
    }

    /** @see CwtDataTypes.EnumValue */
    class ForEnumValue : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.EnumValue

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
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
    }

    /** @see CwtDataTypes.UnionValue */
    class ForUnionValue : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
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
    }

    /** @see CwtDataTypes.DynamicValue */
    class ForDynamicValue : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.DynamicValue

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            val name = context.expression.value.substringBefore('@')
            if (!name.isIdentifier(".")) return ParadoxMatchResult.NotMatch
            val dynamicValueType = configExpression.metadata.value
            if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResult.FallbackMatch
        }
    }

    /** @see CwtDataTypes.ScopeField */
    class ForScopeField : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.ScopeField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forScopeFieldExpression(context.element, context.configGroup, context.expression.value, configExpression)
        }
    }

    /** @see CwtDataTypes.ValueField */
    class ForValueField : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.ValueField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
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
    }

    /** @see CwtDataTypes.VariableField */
    class ForVariableField : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.VariableField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
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
    }

    /** @see CwtDataTypes.Modifier */
    class ForModifier : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Modifier

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            if (!context.expression.value.isIdentifier()) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResultFactory.forModifier(context.element, context.configGroup, context.expression.value)
        }
    }

    /** @see CwtDataTypes.Command */
    class ForCommand : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Command

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            return ParadoxMatchResult.NotMatch // TODO 2.1.1+ 目前不支持用来匹配脚本表达式
        }
    }

    /** @see CwtDataTypes.Template */
    class ForTemplate : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Template

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forTemplate(context.element, context.configGroup, context.expression.value, configExpression, context.options)
        }
    }

    /** @see CwtDataTypes.ScriptValueReference */
    class ForScriptValueReference : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.ScriptValueReference

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forScriptValueReferenceExpression(context.configGroup, context.expression.value)
        }
    }

    /** @see CwtDataTypes.DefineReference */
    class ForDefineReference : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.DefineReference

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forDefineReferenceExpression(context.configGroup, context.expression.value)
        }
    }

    /** @see CwtDataTypes.ArrayDefineReference */
    class ForArrayDefineReference : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.ArrayDefineReference

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forArrayDefineReferenceExpression(context.configGroup, context.expression.value)
        }
    }

    /** @see CwtDataTypes.Tags */
    class ForTags : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Tags

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (config == null) return ParadoxMatchResult.NotMatch
            if (context.expression.value.isEmpty()) return ParadoxMatchResult.FallbackMatch // 2.1.10 compatible
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forTagsExpression(context.configGroup, context.expression.value, config)
        }
    }

    /** @see CwtDataTypes.DatabaseObject */
    class ForDatabaseObject : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.DatabaseObject

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forDatabaseObjectExpression(context.configGroup, context.expression.value)
        }
    }

    /** @see CwtDataTypes.NameFormat */
    class ForNameFormat : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.NameFormat

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (config == null) return ParadoxMatchResult.NotMatch
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResultFactory.forNameFormatExpression(context.configGroup, context.expression.value, config)
        }
    }

    /** @see CwtDataTypes.TechnologyWithLevel */
    class ForTechnologyWithLevel : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.TechnologyWithLevel

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            if (context.expression.value.length > 1 && context.expression.value.indexOf('@') >= 1) return ParadoxMatchResult.WildcardMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Parameter */
    class ForParameter : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Parameter

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // 匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
            // 3.0.3 必须形如标识符（不允许带参数）
            if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.NotMatch
            if (!context.expression.value.isIdentifier()) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResult.ExactMatch
        }
    }

    /** @see CwtDataTypes.ParameterValue */
    class ForParameterValue : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.ParameterValue

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // 匹配参数值（只要不是子句即可匹配）
            if (context.expression.type == ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResult.ExactMatch
        }
    }

    /** @see CwtDataTypes.LocalisationParameter */
    class ForLocalisationParameter : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.LocalisationParameter

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // 匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
            // 3.0.3 必须形如标识符（可以带参数）
            if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            if (!context.expression.value.isIdentifier(".-'")) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResult.ExactMatch
        }
    }
}
