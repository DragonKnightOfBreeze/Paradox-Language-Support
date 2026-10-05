package icu.windea.pls.ep.config.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.scopes.CwtDataExpressionMetadataResolutionScope
import icu.windea.pls.config.scopes.CwtDataExpressionMetadataResolutionScope.INSTANCE.resolveValue
import icu.windea.pls.core.removeSurroundingOrNull

class CwtBasicDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport(), CwtDataExpressionMetadataResolutionScope {
    override fun registerProviders() {
        register(CwtDataTypes.Any, $$"$any")
        register(CwtDataTypes.Literal, $$"$literal")

        register(CwtDataTypes.WildcardAny, $$$"$$any")
        register(CwtDataTypes.WildcardLiteral, $$$"$$literal")

        register(CwtDataTypes.Bool, "bool")

        register(CwtDataTypes.Int, "int")
        registerRanged(CwtDataTypes.Int, "int") { intRange = it.resolveIntRange() }

        register(CwtDataTypes.Float, "float")
        registerRanged(CwtDataTypes.Float, "float") { floatRange = it.resolveFloatRange() }

        register(CwtDataTypes.Scalar, "scalar")

        register(CwtDataTypes.ColorField, "colour_field")
        register(CwtDataTypes.ColorField, "colour[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.ColorField, "color_field")
        register(CwtDataTypes.ColorField, "color[", "]") { value = it.resolveValue() }
    }
}

class CwtExtraBasicDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport(), CwtDataExpressionMetadataResolutionScope {
    override fun registerProviders() {
        register(CwtDataTypes.PercentageField, "percentage_field")
        register(CwtDataTypes.IntPercentageField, "int_percentage_field")

        register(CwtDataTypes.DateField, "date_field")
        register(CwtDataTypes.DateField, "date_field[", "]") { value = it.resolveValue() }
    }
}

class CwtCoreDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport(), CwtDataExpressionMetadataResolutionScope {
    override fun registerProviders() {
        register(CwtDataTypes.Localisation, "localisation")
        register(CwtDataTypes.SyncedLocalisation, "localisation_synced")
        register(CwtDataTypes.InlineLocalisation, "localisation_inline")

        register(CwtDataTypes.Modifier, "<modifier>")
        register(CwtDataTypes.Definition, "<", ">") { value = it.resolveValue() }

        register(CwtDataTypes.Value, "value[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.ValueSet, "value_set[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.DynamicValue, "dynamic_value[", "]") { value = it.resolveValue() }

        register(CwtDataTypes.EnumValue, "enum[", "]") { value = it.resolveValue() }

        register(CwtDataTypes.ScopeField, "scope_field")
        register(CwtDataTypes.Scope, "scope[", "]") { value = it.resolveValue().takeIf { v -> v != "any" } }
        register(CwtDataTypes.ScopeGroup, "scope_group[", "]") { value = it.resolveValue() }

        register(CwtDataTypes.ValueField, "value_field")
        registerRanged(CwtDataTypes.ValueField, "value_field") { floatRange = it.resolveFloatRange() }
        register(CwtDataTypes.IntValueField, "int_value_field")
        registerRanged(CwtDataTypes.IntValueField, "int_value_field") { intRange = it.resolveIntRange() }

        register(CwtDataTypes.VariableField, "variable_field")
        registerRanged(CwtDataTypes.VariableField, "variable_field") { floatRange = it.resolveFloatRange() }
        register(CwtDataTypes.VariableField, "variable_field_32")
        registerRanged(CwtDataTypes.VariableField, "variable_field_32") { floatRange = it.resolveFloatRange() }
        register(CwtDataTypes.IntVariableField, "int_variable_field")
        registerRanged(CwtDataTypes.IntVariableField, "int_variable_field") { intRange = it.resolveIntRange() }
        register(CwtDataTypes.IntVariableField, "int_variable_field_32")
        registerRanged(CwtDataTypes.IntVariableField, "int_variable_field_32") { intRange = it.resolveIntRange() }

        register(CwtDataTypes.Command, $$"$command")
        register(CwtDataTypes.ScriptValueReference, $$"$script_value_reference")
        register(CwtDataTypes.DefineReference, $$"$define_reference")
        register(CwtDataTypes.ArrayDefineReference, $$"$array_define_reference")
        register(CwtDataTypes.Tags, $$"$tags[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.Tags, $$"$tags_condition[", "]") { value = it.resolveValue(); condition = true }
        register(CwtDataTypes.DatabaseObject, $$"$database_object")
        register(CwtDataTypes.NameFormat, "name_format[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.NameFormat, $$"$name_format[", "]") { value = it.resolveValue() } // for alignment

        register(CwtDataTypes.TechnologyWithLevel, $$"$technology_with_level")

        register(CwtDataTypes.Parameter, $$"$parameter")
        register(CwtDataTypes.ParameterValue, $$"$parameter_value")
        register(CwtDataTypes.LocalisationParameter, $$"$localisation_parameter")
    }
}

class CwtPathReferenceDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport(), CwtDataExpressionMetadataResolutionScope {
    override fun registerProviders() {
        register(CwtDataTypes.FileName, "filename")
        register(CwtDataTypes.FileName, "filename[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.FilePath, "filepath")
        register(CwtDataTypes.FilePath, "filepath[./]") { value = "./" } // fixed (should keep `"./"`)
        register(CwtDataTypes.FilePath, "filepath[", "]") { value = it.resolvePath() }
        register(CwtDataTypes.Icon, "icon[", "]") { value = it.resolvePath() }
        register(CwtDataTypes.AbsoluteFilePath, "abs_filepath")
    }
}

class CwtExternalReferenceDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport(), CwtDataExpressionMetadataResolutionScope {
    override fun registerProviders() {
        register(CwtDataTypes.ShaderEffect, $$"$shader_effect")
        register(CwtDataTypes.MeshLocator, $$"$mesh_locator")
    }
}

class CwtExpandableDataExpressionSupport : CwtTextPatternBasedDataExpressionSupport() {
    override fun registerProviders() {
        register(CwtDataTypes.UnionValue, "union[", "]") { value = it.resolveValue() }

        register(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.AliasName, "alias_name[", "]") { value = it.resolveValue() }
        register(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]") { value = it.resolveValue() }

        register(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]") { value = it.resolveValue() }
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

class CwtSnippetDataExpressionSupport : CwtDataExpressionSupport,CwtDataExpressionMetadataResolutionScope {
    override fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        val separatorIndex = expressionString.indexOf('|')
        if (separatorIndex == -1) return null
        val text = expressionString.substring(0, separatorIndex)
        val templatesText = expressionString.substring(separatorIndex + 1)
        run {
            val input = text.removeSurroundingOrNull("<", ">") ?: return@run
            return CwtDataExpression.create(expressionString, CwtDataTypes.DefinitionSnippet, role) {
                value = input.resolveValue()
                snippetTemplates = templatesText.resolveSnippetTemplates()
            }
        }
        run {
            if (text != "localisation") return@run
            return CwtDataExpression.create(expressionString, CwtDataTypes.LocalisationSnippet, role) {
                snippetTemplates = templatesText.resolveSnippetTemplates()
            }
        }
        return null
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null // explicitly unsupported
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

