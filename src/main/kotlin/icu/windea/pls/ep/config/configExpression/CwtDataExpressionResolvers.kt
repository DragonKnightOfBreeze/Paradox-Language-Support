package icu.windea.pls.ep.config.configExpression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configExpression.condition
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configExpression.wildcard
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo

class CwtBasicDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport() {
    override fun registerProviders() {
        fromLiteral(CwtDataTypes.Any, "\$any")

        fromLiteral(CwtDataTypes.Bool, "bool")

        fromLiteral(CwtDataTypes.Int, "int")
        fromRanged(CwtDataTypes.Int, "int") { intRange = IntRangeInfo.from(it) }

        fromLiteral(CwtDataTypes.Float, "float")
        fromRanged(CwtDataTypes.Float, "float") { floatRange = FloatRangeInfo.from(it) }

        fromLiteral(CwtDataTypes.Scalar, "scalar")
        fromLiteral(CwtDataTypes.Scalar, "wildcard_scalar") { wildcard = true }

        fromLiteral(CwtDataTypes.ColorField, "colour_field")
        fromParameterized(CwtDataTypes.ColorField, "colour[", "]") { value = it.orNull() }
        fromLiteral(CwtDataTypes.ColorField, "color_field")
        fromParameterized(CwtDataTypes.ColorField, "color[", "]") { value = it.orNull() }
    }
}

class CwtExtraBasicDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport() {
    override fun registerProviders() {
        fromLiteral(CwtDataTypes.PercentageField, "percentage_field")
        fromLiteral(CwtDataTypes.IntPercentageField, "int_percentage_field")

        fromLiteral(CwtDataTypes.DateField, "date_field")
        fromParameterized(CwtDataTypes.DateField, "date_field[", "]") { value = it.orNull() }
    }
}

class CwtCoreDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport() {
    override fun registerProviders() {
        fromLiteral(CwtDataTypes.Localisation, "localisation")
        fromLiteral(CwtDataTypes.SyncedLocalisation, "localisation_synced")
        fromLiteral(CwtDataTypes.InlineLocalisation, "localisation_inline")

        fromLiteral(CwtDataTypes.FileName, "filename")
        fromParameterized(CwtDataTypes.FileName, "filename[", "]") { value = it.orNull() }
        fromLiteral(CwtDataTypes.FilePath, "filepath")
        fromLiteral(CwtDataTypes.FilePath, "filepath[./]") { value = "./" } // fixed (should keep `"./"`)
        fromParameterized(CwtDataTypes.FilePath, "filepath[", "]") { value = it.optimizedPath().orNull() }
        fromParameterized(CwtDataTypes.Icon, "icon[", "]") { value = it.optimizedPath().orNull() }
        fromLiteral(CwtDataTypes.AbsoluteFilePath, "abs_filepath")

        fromLiteral(CwtDataTypes.Modifier, "<modifier>")
        fromParameterized(CwtDataTypes.Definition, "<", ">") { value = it.orNull() }

        fromParameterized(CwtDataTypes.Value, "value[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.ValueSet, "value_set[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.DynamicValue, "dynamic_value[", "]") { value = it.orNull() }

        fromParameterized(CwtDataTypes.EnumValue, "enum[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.ScopeField, "scope_field")
        fromParameterized(CwtDataTypes.Scope, "scope[", "]") { value = it.orNull().takeIf { v -> v != "any" } }
        fromParameterized(CwtDataTypes.ScopeGroup, "scope_group[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.ValueField, "value_field")
        fromRanged(CwtDataTypes.ValueField, "value_field") { floatRange = FloatRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.IntValueField, "int_value_field")
        fromRanged(CwtDataTypes.IntValueField, "int_value_field") { intRange = IntRangeInfo.from(it) }

        fromLiteral(CwtDataTypes.VariableField, "variable_field")
        fromRanged(CwtDataTypes.VariableField, "variable_field") { floatRange = FloatRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.VariableField, "variable_field_32")
        fromRanged(CwtDataTypes.VariableField, "variable_field_32") { floatRange = FloatRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.IntVariableField, "int_variable_field")
        fromRanged(CwtDataTypes.IntVariableField, "int_variable_field") { intRange = IntRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.IntVariableField, "int_variable_field_32")
        fromRanged(CwtDataTypes.IntVariableField, "int_variable_field_32") { intRange = IntRangeInfo.from(it) }

        fromParameterized(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.AliasName, "alias_name[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.Command, "\$command")
        fromLiteral(CwtDataTypes.ScriptValueReference, "\$script_value_reference")
        fromLiteral(CwtDataTypes.DefineReference, "\$define_reference")
        fromLiteral(CwtDataTypes.ArrayDefineReference, "\$array_define_reference")
        fromParameterized(CwtDataTypes.Tags, "\$tags[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.Tags, "\$tags_condition[", "]") { value = it.orNull(); condition = true }
        fromLiteral(CwtDataTypes.DatabaseObject, "\$database_object")
        fromParameterized(CwtDataTypes.NameFormat, "name_format[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.ShaderEffect, "\$shader_effect")
        fromLiteral(CwtDataTypes.MeshLocator, "\$mesh_locator")
        fromLiteral(CwtDataTypes.TechnologyWithLevel, "\$technology_with_level")

        fromLiteral(CwtDataTypes.Parameter, "\$parameter")
        fromLiteral(CwtDataTypes.ParameterValue, "\$parameter_value")
        fromLiteral(CwtDataTypes.LocalisationParameter, "\$localisation_parameter")

        fromParameterized(CwtDataTypes.UnionValue, "union[", "]") { value = it.orNull() }
    }
}

class CwtConstantDataExpressionSupport : CwtDataExpressionSupport {
    private val forceRegex = """\w*\[[\w:]*]""".toRegex() // `type[x]`, `alias[x:y]`, etc.
    private val excludeCharacters = ":.@[]<>".toCharArray() // `x_<y>_enum[z]`, etc.

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (expressionString.any { c -> c in excludeCharacters } && !forceRegex.matches(expressionString)) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Constant).apply { value = expressionString }
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtTemplateDataExpressionSupport : CwtDataExpressionSupport {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (CwtTemplateExpression.resolve(expressionString).expressionString.isEmpty()) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TemplateExpression).apply { value = expressionString }
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtPatternDataExpressionSupport : CwtDataExpressionSupport {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        doResolve(expressionString, isKey, CwtDataTypes.Glob, "glob:", false)?.let { return it }
        doResolve(expressionString, isKey, CwtDataTypes.Glob, "glob.i:", true)?.let { return it }
        doResolve(expressionString, isKey, CwtDataTypes.Ant, "ant:", false)?.let { return it }
        doResolve(expressionString, isKey, CwtDataTypes.Ant, "ant.i:", true)?.let { return it }
        doResolve(expressionString, isKey, CwtDataTypes.Regex, "re:", false)?.let { return it }
        doResolve(expressionString, isKey, CwtDataTypes.Regex, "re.i:", true)?.let { return it }
        doResolve(expressionString, isKey, CwtDataTypes.Regex, "regex:", false)?.let { return it } // for compatibility
        doResolve(expressionString, isKey, CwtDataTypes.Regex, "regex.i:", true)?.let { return it } // for compatibility
        return null
    }

    private fun doResolve(expressionString: String, isKey: Boolean, dataType: CwtDataType, prefix: String, ignoreCase: Boolean): CwtDataExpression? {
        val v = expressionString.removePrefixOrNull(prefix) ?: return null
        return CwtDataExpression.create(expressionString, isKey, dataType).apply { value = v }.apply { this.ignoreCase = ignoreCase }
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtSuffixAwareDataExpressionSupport : CwtDataExpressionSupport {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        val separatorIndex = expressionString.indexOf('|')
        if (separatorIndex == -1) return null
        val text = expressionString.substring(0, separatorIndex)
        val suffixes = expressionString.substring(separatorIndex + 1).toCommaDelimitedStringSet()
        run {
            val t = text.removeSurroundingOrNull("<", ">") ?: return@run
            if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Definition).apply { value = t.orNull() }
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareDefinition).apply { value = t.orNull() }.apply { this.suffixes = suffixes }
        }
        run {
            if (text != "localisation") return@run
            if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Localisation)
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareLocalisation).apply { this.suffixes = suffixes }
        }
        run {
            if (text != "localisation_synced") return@run
            if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SyncedLocalisation)
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareSyncedLocalisation).apply { this.suffixes = suffixes }
        }
        return null
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}
