package icu.windea.pls.ep.dataExpression

import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*

abstract class RuleBasedCwtDataExpressionResolver : CwtDataExpressionResolver {
    sealed interface Rule {
        val type: CwtDataType
    }

    data class ConstantRule(
        override val type: CwtDataType,
        val constant: String,
        val value: String? = null,
        val extraValue: Any? = null,
    ) : Rule

    data class DynamicRule(
        override val type: CwtDataType,
        val prefix: String = "",
        val suffix: String = "",
        val valueResolver: ((data: String) -> String?)? = null,
        val extraValueResolver: ((data: String) -> Any?)? = null,
    ) : Rule

    protected fun rule(
        type: CwtDataType,
        constant: String,
        value: String? = null,
        extraValue: Any? = null,
    ): Rule {
        return ConstantRule(type, constant, value, extraValue)
    }

    protected fun rule(
        type: CwtDataType,
        prefix: String,
        suffix: String,
        valueResolver: ((data: String) -> String?)? = null,
        extraValueResolver: ((data: String) -> Any?)? = null,
    ): Rule {
        return DynamicRule(type, prefix, suffix, valueResolver, extraValueResolver)
    }

    abstract val rules: List<Rule>

    private val constantRuleMap by lazy { rules.filterIsInstance<ConstantRule>().associateBy { it.constant } }
    private val dynamicRules by lazy { rules.filterIsInstance<DynamicRule>() }

    final override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        run {
            val rule = constantRuleMap[expressionString]
            if (rule == null) return@run
            return CwtDataExpression.create(expressionString, isKey, rule.type, rule.value, rule.extraValue)
        }
        run {
            dynamicRules.forEach f@{ rule ->
                val data = expressionString.removeSurroundingOrNull(rule.prefix, rule.suffix)
                if (data == null) return@f
                val value = rule.valueResolver?.invoke(data)
                val extraValue = rule.extraValueResolver?.invoke(data)
                return CwtDataExpression.create(expressionString, isKey, rule.type, value, extraValue)
            }
        }
        return null
    }
}

class BaseCwtDataExpressionResolver : RuleBasedCwtDataExpressionResolver() {
    @Suppress("MoveLambdaOutsideParentheses")
    override val rules = listOf(
        rule(CwtDataTypes.Bool, "bool"),

        rule(CwtDataTypes.Int, "int"),
        rule(CwtDataTypes.Int, "int[", "]", null, { it.split("..", limit = 2).let { v -> tupleOf(v.getOrNull(0)?.toIntOrNull(), v.getOrNull(1)?.toIntOrNull()) } }),

        rule(CwtDataTypes.Float, "float"),
        rule(CwtDataTypes.Float, "float[", "]", null, { it.split("..", limit = 2).let { v -> tupleOf(v.getOrNull(0)?.toFloatOrNull(), v.getOrNull(1)?.toFloatOrNull()) } }),

        rule(CwtDataTypes.Scalar, "scalar"),

        rule(CwtDataTypes.ColorField, "colour_field"),
        rule(CwtDataTypes.ColorField, "colour_field[", "]", { it.orNull() }),
        rule(CwtDataTypes.ColorField, "color_field"),
        rule(CwtDataTypes.ColorField, "color_field[", "]", { it.orNull() }),
    )
}

class CoreCwtDataExpressionResolver : RuleBasedCwtDataExpressionResolver() {
    override val rules: List<Rule> = listOf(
        rule(CwtDataTypes.PercentageField, "percentage_field"),
        rule(CwtDataTypes.DateField, "date_field"),

        rule(CwtDataTypes.Localisation, "localisation"),
        rule(CwtDataTypes.SyncedLocalisation, "localisation_synced"),
        rule(CwtDataTypes.InlineLocalisation, "localisation_inline"),

        rule(CwtDataTypes.AbsoluteFilePath, "abs_filepath"),
        rule(CwtDataTypes.FileName, "filename"),
        rule(CwtDataTypes.FileName, "filename[", "]", { it.orNull() }),
        rule(CwtDataTypes.FilePath, "filepath"),
        rule(CwtDataTypes.FilePath, "filepath[", "]", { it.removePrefix("game/").orNull() }),
        rule(CwtDataTypes.Icon, "icon[", "]", { it.removePrefix("game/").orNull() }),

        rule(CwtDataTypes.Modifier, "<modifier>"),
        rule(CwtDataTypes.TechnologyWithLevel, "<technology_with_level>"),
        rule(CwtDataTypes.Definition, "<", ">", { it.orNull() }),

        rule(CwtDataTypes.Value, "value[", "]", { it.orNull() }),
        rule(CwtDataTypes.ValueSet, "value_set[", "]", { it.orNull() }),
        rule(CwtDataTypes.DynamicValue, "dynamic_value[", "]", { it.orNull() }),

        rule(CwtDataTypes.EnumValue, "enum[", "]", { it.orNull() }),

        rule(CwtDataTypes.ScopeField, "scope_field"),
        rule(CwtDataTypes.Scope, "scope[", "]", { it.orNull().takeIf { v -> v != "any" } }),
        rule(CwtDataTypes.ScopeGroup, "scope_group[", "]", { it.orNull() }),

        rule(CwtDataTypes.ValueField, "value_field"),
        rule(CwtDataTypes.ValueField, "value_field[", "]", { it.orNull() }),
        rule(CwtDataTypes.IntValueField, "int_value_field"),
        rule(CwtDataTypes.IntValueField, "int_value_field[", "]", { it.orNull() }),

        rule(CwtDataTypes.VariableField, "variable_field"),
        rule(CwtDataTypes.VariableField, "variable_field[", "]", { it.orNull() }),
        rule(CwtDataTypes.VariableField, "variable_field32"),
        rule(CwtDataTypes.VariableField, "variable_field32[", "]", { it.orNull() }),
        rule(CwtDataTypes.IntVariableField, "int_variable_field"),
        rule(CwtDataTypes.IntVariableField, "int_variable_field[", "]", { it.orNull() }),
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32"),
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32[", "]", { it.orNull() }),

        rule(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]", { it.orNull() }),
        rule(CwtDataTypes.AliasName, "alias_name[", "]", { it.orNull() }),
        rule(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]", { it.orNull() }),
        rule(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]", { it.orNull() }),

        rule(CwtDataTypes.Any, "\$any"),

        rule(CwtDataTypes.Parameter, "\$parameter"),
        rule(CwtDataTypes.ParameterValue, "\$parameter_value"),
        rule(CwtDataTypes.LocalisationParameter, "\$localisation_parameter"),
        rule(CwtDataTypes.ShaderEffect, "\$shader_effect"),
        rule(CwtDataTypes.DatabaseObject, "\$database_object"),
        rule(CwtDataTypes.DefineReference, "\$define_reference"),
        rule(CwtDataTypes.StellarisNameFormat, "stellaris_name_format[", "]", { it.orNull() }),
    )
}

class ConstantCwtDataExpressionResolver : CwtDataExpressionResolver, CwtConfigPatternAware {
    private val excludeCharacters = ":.@[]<>".toCharArray()

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (expressionString.any { c -> c in excludeCharacters }) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Constant, expressionString)
    }
}

class TemplateExpressionCwtDataExpressionResolver : CwtDataExpressionResolver, CwtConfigPatternAware {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (CwtTemplateExpression.resolve(expressionString).expressionString.isEmpty()) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TemplateExpression)
    }
}

class AntExpressionCwtDataExpressionResolver : CwtDataExpressionResolver, CwtConfigPatternAware {
    private val prefix = "ant:"
    private val prefixIgnoreCase = "ant.i:"

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        expressionString.removePrefixOrNull(prefix)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AntExpression, v.orNull())
        }
        expressionString.removePrefixOrNull(prefixIgnoreCase)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AntExpression, v.orNull(), true)
        }
        return null
    }
}

class RegexCwtDataExpressionResolver : CwtDataExpressionResolver, CwtConfigPatternAware {
    private val prefix = "re:"
    private val prefixIgnoreCase = "re.i:"

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        expressionString.removePrefixOrNull(prefix)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Regex, v.orNull())
        }
        expressionString.removePrefixOrNull(prefixIgnoreCase)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Regex, v.orNull(), true)
        }
        return null
    }
}
