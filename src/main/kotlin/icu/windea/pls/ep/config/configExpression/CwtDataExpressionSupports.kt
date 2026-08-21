package icu.windea.pls.ep.config.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.toDelimitedSet
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo

class CwtBasicDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport() {
    override fun registerProviders() {
        register(CwtDataTypes.Any, "\$any")

        register(CwtDataTypes.Bool, "bool")

        register(CwtDataTypes.Int, "int")
        registerRanged(CwtDataTypes.Int, "int") { intRange = IntRangeInfo.from(it) }

        register(CwtDataTypes.Float, "float")
        registerRanged(CwtDataTypes.Float, "float") { floatRange = FloatRangeInfo.from(it) }

        register(CwtDataTypes.Scalar, "scalar")
        register(CwtDataTypes.Scalar, "wildcard_scalar") { wildcard = true }

        register(CwtDataTypes.ColorField, "colour_field")
        register(CwtDataTypes.ColorField, "colour[", "]") { value = it.orNull() }
        register(CwtDataTypes.ColorField, "color_field")
        register(CwtDataTypes.ColorField, "color[", "]") { value = it.orNull() }
    }
}

class CwtExtraBasicDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport() {
    override fun registerProviders() {
        register(CwtDataTypes.PercentageField, "percentage_field")
        register(CwtDataTypes.IntPercentageField, "int_percentage_field")

        register(CwtDataTypes.DateField, "date_field")
        register(CwtDataTypes.DateField, "date_field[", "]") { value = it.orNull() }
    }
}

class CwtCoreDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport(), CwtConfigResolverScope {
    override fun registerProviders() {
        register(CwtDataTypes.Localisation, "localisation")
        register(CwtDataTypes.SyncedLocalisation, "localisation_synced")
        register(CwtDataTypes.InlineLocalisation, "localisation_inline")

        register(CwtDataTypes.FileName, "filename")
        register(CwtDataTypes.FileName, "filename[", "]") { value = it.orNull() }
        register(CwtDataTypes.FilePath, "filepath")
        register(CwtDataTypes.FilePath, "filepath[./]") { value = "./" } // fixed (should keep `"./"`)
        register(CwtDataTypes.FilePath, "filepath[", "]") { value = it.optimizedPath().orNull() }
        register(CwtDataTypes.Icon, "icon[", "]") { value = it.optimizedPath().orNull() }
        register(CwtDataTypes.AbsoluteFilePath, "abs_filepath")

        register(CwtDataTypes.Modifier, "<modifier>")
        register(CwtDataTypes.Definition, "<", ">") { value = it.orNull() }

        register(CwtDataTypes.Value, "value[", "]") { value = it.orNull() }
        register(CwtDataTypes.ValueSet, "value_set[", "]") { value = it.orNull() }
        register(CwtDataTypes.DynamicValue, "dynamic_value[", "]") { value = it.orNull() }

        register(CwtDataTypes.EnumValue, "enum[", "]") { value = it.orNull() }

        register(CwtDataTypes.UnionValue, "union[", "]") { value = it.orNull() }

        register(CwtDataTypes.ScopeField, "scope_field")
        register(CwtDataTypes.Scope, "scope[", "]") { value = it.orNull().takeIf { v -> v != "any" } }
        register(CwtDataTypes.ScopeGroup, "scope_group[", "]") { value = it.orNull() }

        register(CwtDataTypes.ValueField, "value_field")
        registerRanged(CwtDataTypes.ValueField, "value_field") { floatRange = FloatRangeInfo.from(it) }
        register(CwtDataTypes.IntValueField, "int_value_field")
        registerRanged(CwtDataTypes.IntValueField, "int_value_field") { intRange = IntRangeInfo.from(it) }

        register(CwtDataTypes.VariableField, "variable_field")
        registerRanged(CwtDataTypes.VariableField, "variable_field") { floatRange = FloatRangeInfo.from(it) }
        register(CwtDataTypes.VariableField, "variable_field_32")
        registerRanged(CwtDataTypes.VariableField, "variable_field_32") { floatRange = FloatRangeInfo.from(it) }
        register(CwtDataTypes.IntVariableField, "int_variable_field")
        registerRanged(CwtDataTypes.IntVariableField, "int_variable_field") { intRange = IntRangeInfo.from(it) }
        register(CwtDataTypes.IntVariableField, "int_variable_field_32")
        registerRanged(CwtDataTypes.IntVariableField, "int_variable_field_32") { intRange = IntRangeInfo.from(it) }

        register(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]") { value = it.orNull() }
        register(CwtDataTypes.AliasName, "alias_name[", "]") { value = it.orNull() }
        register(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]") { value = it.orNull() }
        register(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]") { value = it.orNull() }

        register(CwtDataTypes.Command, "\$command")
        register(CwtDataTypes.ScriptValueReference, "\$script_value_reference")
        register(CwtDataTypes.DefineReference, "\$define_reference")
        register(CwtDataTypes.ArrayDefineReference, "\$array_define_reference")
        register(CwtDataTypes.Tags, "\$tags[", "]") { value = it.orNull() }
        register(CwtDataTypes.Tags, "\$tags_condition[", "]") { value = it.orNull(); condition = true }
        register(CwtDataTypes.DatabaseObject, "\$database_object")
        register(CwtDataTypes.NameFormat, "name_format[", "]") { value = it.orNull() }

        register(CwtDataTypes.ShaderEffect, "\$shader_effect")
        register(CwtDataTypes.MeshLocator, "\$mesh_locator")
        register(CwtDataTypes.TechnologyWithLevel, "\$technology_with_level")

        register(CwtDataTypes.Parameter, "\$parameter")
        register(CwtDataTypes.ParameterValue, "\$parameter_value")
        register(CwtDataTypes.LocalisationParameter, "\$localisation_parameter")
    }
}

class CwtConstantDataExpressionSupport : CwtDataExpressionSupport {
    private val forceRegex = """\w*\[[\w:]*]""".toRegex() // `type[x]`, `alias[x:y]`, etc.
    private val excludeCharacters = ":.@[]<>".toCharArray() // `x_<y>_enum[z]`, etc.

    override fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        if (expressionString.any { c -> c in excludeCharacters } && !forceRegex.matches(expressionString)) return null
        return CwtDataExpression.create(expressionString, CwtDataTypes.Constant, role)
    }
}

class CwtTemplateDataExpressionSupport : CwtDataExpressionSupport {
    override fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        if (CwtTemplateExpression.resolve(expressionString).expressionString.isEmpty()) return null
        return CwtDataExpression.create(expressionString, CwtDataTypes.Template, role)
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null // explicitly unsupported
    }
}

class CwtPatternDataExpressionSupport : CwtPrefixBasedDataExpressionSupport() {
    override fun registerProviders() {
        register(CwtDataTypes.Glob, "glob:", false)
        register(CwtDataTypes.Glob, "glob.i:", true)
        register(CwtDataTypes.Ant, "ant:", false)
        register(CwtDataTypes.Ant, "ant.i:", true)
        register(CwtDataTypes.Regex, "re:", false)
        register(CwtDataTypes.Regex, "re.i:", true)
        register(CwtDataTypes.Regex, "regex:", false) // for compatibility
        register(CwtDataTypes.Regex, "regex.i:", true) // for compatibility
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null // explicitly unsupported
    }
}

class CwtSuffixAwareDataExpressionSupport : CwtDataExpressionSupport {
    override fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        val separatorIndex = expressionString.indexOf('|')
        if (separatorIndex == -1) return null
        val text = expressionString.substring(0, separatorIndex)
        val expectedSuffixes = expressionString.substring(separatorIndex + 1).toDelimitedSet()
        run {
            val t = text.removeSurroundingOrNull("<", ">") ?: return@run
            if (expectedSuffixes.isEmpty()) return CwtDataExpression.create(expressionString, CwtDataTypes.Definition, role) { value = t.orNull() }
            return CwtDataExpression.create(expressionString, CwtDataTypes.SuffixAwareDefinition, role) { value = t.orNull(); suffixes = expectedSuffixes.optimized() }
        }
        run {
            if (text != "localisation") return@run
            if (expectedSuffixes.isEmpty()) return CwtDataExpression.create(expressionString, CwtDataTypes.Localisation, role)
            return CwtDataExpression.create(expressionString, CwtDataTypes.SuffixAwareLocalisation, role) { suffixes = expectedSuffixes.optimized() }
        }
        run {
            if (text != "localisation_synced") return@run
            if (expectedSuffixes.isEmpty()) return CwtDataExpression.create(expressionString, CwtDataTypes.SyncedLocalisation, role)
            return CwtDataExpression.create(expressionString, CwtDataTypes.SuffixAwareSyncedLocalisation, role) { suffixes = expectedSuffixes.optimized() }
        }
        return null
    }
}
