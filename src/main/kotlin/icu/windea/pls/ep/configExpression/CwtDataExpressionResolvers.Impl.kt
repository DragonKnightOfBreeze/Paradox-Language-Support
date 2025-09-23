package icu.windea.pls.ep.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo

class BaseCwtDataExpressionResolver : RuleBasedCwtDataExpressionResolver() {
    override val rules = listOf(
        rule(CwtDataTypes.Bool, "bool"),

        rule(CwtDataTypes.Int, "int"),
        rule(CwtDataTypes.Int, "int[", "") { intRange = IntRangeInfo.from("[$it") },
        rule(CwtDataTypes.Int, "int(", "") { intRange = IntRangeInfo.from("($it") },

        rule(CwtDataTypes.Float, "float"),
        rule(CwtDataTypes.Float, "float[", "") { floatRange = FloatRangeInfo.from("[$it") },
        rule(CwtDataTypes.Float, "float(", "") { floatRange = FloatRangeInfo.from("($it") },

        rule(CwtDataTypes.Scalar, "scalar"),

        rule(CwtDataTypes.ColorField, "colour_field"),
        rule(CwtDataTypes.ColorField, "colour[", "]") { value = it.orNull() },
        rule(CwtDataTypes.ColorField, "color_field"),
        rule(CwtDataTypes.ColorField, "color[", "]") { value = it.orNull() },
    )
}

class CoreCwtDataExpressionResolver : RuleBasedCwtDataExpressionResolver() {
    override val rules: List<Rule> = listOf(
        rule(CwtDataTypes.PercentageField, "percentage_field"),
        rule(CwtDataTypes.DateField, "date_field"),
        rule(CwtDataTypes.DateField, "date_field[", "]") { value = it.orNull() },

        rule(CwtDataTypes.Localisation, "localisation"),
        rule(CwtDataTypes.SyncedLocalisation, "localisation_synced"),
        rule(CwtDataTypes.InlineLocalisation, "localisation_inline"),

        rule(CwtDataTypes.AbsoluteFilePath, "abs_filepath"),
        rule(CwtDataTypes.FileName, "filename"),
        rule(CwtDataTypes.FileName, "filename[", "]") { value = it.orNull() },
        rule(CwtDataTypes.FilePath, "filepath"),
        rule(CwtDataTypes.FilePath, "filepath[", "]") { value = it.removePrefix("game/").orNull() },
        rule(CwtDataTypes.Icon, "icon[", "]") { value = it.removePrefix("game/").orNull() },

        rule(CwtDataTypes.Modifier, "<modifier>"),
        rule(CwtDataTypes.TechnologyWithLevel, "<technology_with_level>"),
        rule(CwtDataTypes.Definition, "<", ">") { value = it.orNull() },

        rule(CwtDataTypes.Value, "value[", "]") { value = it.orNull() },
        rule(CwtDataTypes.ValueSet, "value_set[", "]") { value = it.orNull() },
        rule(CwtDataTypes.DynamicValue, "dynamic_value[", "]") { value = it.orNull() },

        rule(CwtDataTypes.EnumValue, "enum[", "]") { value = it.orNull() },

        rule(CwtDataTypes.ScopeField, "scope_field"),
        rule(CwtDataTypes.Scope, "scope[", "]") { value = it.orNull().takeIf { v -> v != "any" } },
        rule(CwtDataTypes.ScopeGroup, "scope_group[", "]") { value = it.orNull() },

        rule(CwtDataTypes.ValueField, "value_field"),
        rule(CwtDataTypes.ValueField, "value_field[", "]") { value = it.orNull() },
        rule(CwtDataTypes.IntValueField, "int_value_field"),
        rule(CwtDataTypes.IntValueField, "int_value_field[", "]") { value = it.orNull() },

        rule(CwtDataTypes.VariableField, "variable_field"),
        rule(CwtDataTypes.VariableField, "variable_field[", "]") { value = it.orNull() },
        rule(CwtDataTypes.VariableField, "variable_field32"),
        rule(CwtDataTypes.VariableField, "variable_field32[", "]") { value = it.orNull() },
        rule(CwtDataTypes.IntVariableField, "int_variable_field"),
        rule(CwtDataTypes.IntVariableField, "int_variable_field[", "]") { value = it.orNull() },
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32"),
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32[", "]") { value = it.orNull() },

        rule(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]") { value = it.orNull() },
        rule(CwtDataTypes.AliasName, "alias_name[", "]") { value = it.orNull() },
        rule(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]") { value = it.orNull() },
        rule(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]") { value = it.orNull() },

        rule(CwtDataTypes.Any, "\$any"),

        rule(CwtDataTypes.Parameter, "\$parameter"),
        rule(CwtDataTypes.ParameterValue, "\$parameter_value"),
        rule(CwtDataTypes.LocalisationParameter, "\$localisation_parameter"),
        rule(CwtDataTypes.ShaderEffect, "\$shader_effect"),
        rule(CwtDataTypes.DatabaseObject, "\$database_object"),
        rule(CwtDataTypes.DefineReference, "\$define_reference"),
        rule(CwtDataTypes.StellarisNameFormat, "stellaris_name_format[", "]") { value = it.orNull() },
    )
}

class ConstantCwtDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    private val excludeCharacters = ":.@[]<>".toCharArray()

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (expressionString.any { c -> c in excludeCharacters }) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Constant).apply { value = expressionString }
    }
}

class TemplateExpressionCwtDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (CwtTemplateExpression.resolve(expressionString).expressionString.isEmpty()) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TemplateExpression).apply { value = expressionString }
    }
}

class AntExpressionCwtDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    private val prefix = "ant:"
    private val prefixIgnoreCase = "ant.i:"

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        expressionString.removePrefixOrNull(prefix)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AntExpression).apply { value = v.orNull() }
        }
        expressionString.removePrefixOrNull(prefixIgnoreCase)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AntExpression).apply { value = v.orNull() }.apply { ignoreCase = true }
        }
        return null
    }
}

class RegexCwtDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    private val prefix = "re:"
    private val prefixIgnoreCase = "re.i:"

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        expressionString.removePrefixOrNull(prefix)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Regex).apply { value = v.orNull() }
        }
        expressionString.removePrefixOrNull(prefixIgnoreCase)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Regex).apply { value = v.orNull() }.apply { ignoreCase = true }
        }
        return null
    }
}

class SuffixAwareDefinitionCwtDataExpressionResolver : SuffixAwareCwtDataExpressionResolver() {
    override fun doResolve(expressionString: String, text: String, suffixes: Set<String>, isKey: Boolean): CwtDataExpression? {
        val t = text.removeSurroundingOrNull("<", ">") ?: return null
        if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Definition).apply { value = t.orNull() }
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareDefinition).apply { value = t.orNull() }.apply { this.suffixes = suffixes }
    }
}

class SuffixAwareLocalisationCwtDataExpressionResolver : SuffixAwareCwtDataExpressionResolver() {
    override fun doResolve(expressionString: String, text: String, suffixes: Set<String>, isKey: Boolean): CwtDataExpression? {
        if (text != "localisation") return null
        if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Localisation)
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareLocalisation).apply { this.suffixes = suffixes }
    }
}

class SuffixAwareSyncedLocalisationCwtDataExpressionResolver : SuffixAwareCwtDataExpressionResolver() {
    override fun doResolve(expressionString: String, text: String, suffixes: Set<String>, isKey: Boolean): CwtDataExpression? {
        if (text != "localisation_synced") return null
        if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SyncedLocalisation)
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareSyncedLocalisation).apply { this.suffixes = suffixes }
    }
}
