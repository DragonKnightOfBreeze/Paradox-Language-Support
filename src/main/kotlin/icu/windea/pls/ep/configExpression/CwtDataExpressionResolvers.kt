package icu.windea.pls.ep.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo

class CwtBaseDataExpressionResolver : RuleBasedCwtDataExpressionResolver() {
    override val rules = listOf(
        rule(CwtDataTypes.Any, "\$any"),

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

class CwtCoreDataExpressionResolver : RuleBasedCwtDataExpressionResolver() {
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
        rule(CwtDataTypes.FilePath, "filepath[", "]") { value = it.optimizedPath().orNull() },
        rule(CwtDataTypes.Icon, "icon[", "]") { value = it.optimizedPath().orNull() },

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
        rule(CwtDataTypes.ValueField, "value_field[", "") { floatRange = FloatRangeInfo.from("[$it") },
        rule(CwtDataTypes.ValueField, "value_field(", "") { floatRange = FloatRangeInfo.from("($it") },
        rule(CwtDataTypes.IntValueField, "int_value_field"),
        rule(CwtDataTypes.IntValueField, "int_value_field[", "") { intRange = IntRangeInfo.from("[$it") },
        rule(CwtDataTypes.IntValueField, "int_value_field(", "") { intRange = IntRangeInfo.from("($it") },

        rule(CwtDataTypes.VariableField, "variable_field"),
        rule(CwtDataTypes.VariableField, "variable_field[", "") { floatRange = FloatRangeInfo.from("[$it") },
        rule(CwtDataTypes.VariableField, "variable_field(", "") { floatRange = FloatRangeInfo.from("($it") },
        rule(CwtDataTypes.VariableField, "variable_field32"),
        rule(CwtDataTypes.VariableField, "variable_field32[", "") { floatRange = FloatRangeInfo.from("[$it") },
        rule(CwtDataTypes.VariableField, "variable_field32(", "") { floatRange = FloatRangeInfo.from("($it") },
        rule(CwtDataTypes.IntVariableField, "int_variable_field"),
        rule(CwtDataTypes.IntVariableField, "int_variable_field[", "") { intRange = IntRangeInfo.from("[$it") },
        rule(CwtDataTypes.IntVariableField, "int_variable_field(", "") { intRange = IntRangeInfo.from("($it") },
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32"),
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32[", "") { intRange = IntRangeInfo.from("[$it") },
        rule(CwtDataTypes.IntVariableField, "int_variable_field_32(", "") { intRange = IntRangeInfo.from("($it") },

        rule(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]") { value = it.orNull() },
        rule(CwtDataTypes.AliasName, "alias_name[", "]") { value = it.orNull() },
        rule(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]") { value = it.orNull() },
        rule(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]") { value = it.orNull() },

        rule(CwtDataTypes.DatabaseObject, "\$database_object"),
        rule(CwtDataTypes.DefineReference, "\$define_reference"),
        rule(CwtDataTypes.StellarisNameFormat, "stellaris_name_format[", "]") { value = it.orNull() },

        rule(CwtDataTypes.Parameter, "\$parameter"),
        rule(CwtDataTypes.ParameterValue, "\$parameter_value"),
        rule(CwtDataTypes.LocalisationParameter, "\$localisation_parameter"),

        rule(CwtDataTypes.ShaderEffect, "\$shader_effect"),
    )
}

class CwtConstantDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    private val forceRegex = """\w*\[[\w:]*]""".toRegex() // `type[x]`, `alias[x:y]`, etc.
    private val excludeCharacters = ":.@[]<>".toCharArray() // `x_<y>_enum[z]`, etc.

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (expressionString.any { c -> c in excludeCharacters } && !forceRegex.matches(expressionString)) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Constant).apply { value = expressionString }
    }
}

class CwtTemplateDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (CwtTemplateExpression.resolve(expressionString).expressionString.isEmpty()) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TemplateExpression).apply { value = expressionString }
    }
}

class CwtAntDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
    private val prefix = "ant:"
    private val prefixIgnoreCase = "ant.i:"

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        expressionString.removePrefixOrNull(prefix)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Ant).apply { value = v.orNull() }
        }
        expressionString.removePrefixOrNull(prefixIgnoreCase)?.let { v ->
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Ant).apply { value = v.orNull() }.apply { ignoreCase = true }
        }
        return null
    }
}

class CwtRegexDataExpressionResolver : PatternAwareCwtDataExpressionResolver() {
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

class CwtSuffixAwareDefinitionDataExpressionResolver : SuffixAwareCwtDataExpressionResolver() {
    override fun doResolve(expressionString: String, text: String, suffixes: Set<String>, isKey: Boolean): CwtDataExpression? {
        val t = text.removeSurroundingOrNull("<", ">") ?: return null
        if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Definition).apply { value = t.orNull() }
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareDefinition).apply { value = t.orNull() }.apply { this.suffixes = suffixes }
    }
}

class CwtSuffixAwareLocalisationDataExpressionResolver : SuffixAwareCwtDataExpressionResolver() {
    override fun doResolve(expressionString: String, text: String, suffixes: Set<String>, isKey: Boolean): CwtDataExpression? {
        if (text != "localisation") return null
        if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Localisation)
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareLocalisation).apply { this.suffixes = suffixes }
    }
}

class CwtSuffixAwareSyncedLocalisationDataExpressionResolver : SuffixAwareCwtDataExpressionResolver() {
    override fun doResolve(expressionString: String, text: String, suffixes: Set<String>, isKey: Boolean): CwtDataExpression? {
        if (text != "localisation_synced") return null
        if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SyncedLocalisation)
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareSyncedLocalisation).apply { this.suffixes = suffixes }
    }
}
