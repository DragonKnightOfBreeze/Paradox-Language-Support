package icu.windea.pls.ep.expression

import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*

class BaseCwtDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if(expressionString == "bool") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Bool)
        }
        
        if(expressionString == "int") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Int)
        }
        expressionString.removeSurroundingOrNull("int[", "]")?.let { v ->
            val range = v.split("..", limit = 2)
                .let { tupleOf(it.getOrNull(0)?.toIntOrNull(), it.getOrNull(1)?.toIntOrNull()) }
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Int, null, range)
        }
        
        if(expressionString == "float") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Float)
        }
        expressionString.removeSurroundingOrNull("float[", "]")?.let { v ->
            val range = v.split("..", limit = 2)
                .let { tupleOf(it.getOrNull(0)?.toFloatOrNull(), it.getOrNull(1)?.toFloatOrNull()) }
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Float, null, range)
        }
        
        if(expressionString == "scalar") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Scalar)
        }
        
        if(expressionString == "colour_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ColorField)
        }
        expressionString.removeSurroundingOrNull("colour[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ColorField, value)
        }
        
        return null
    }
}

class CoreCwtDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if(expressionString == "percentage_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.PercentageField)
        }
        
        if(expressionString == "date_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.DateField)
        }
        
        if(expressionString == "localisation") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Localisation)
        }
        
        if(expressionString == "localisation_synced") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SyncedLocalisation)
        }
        
        if(expressionString == "localisation_inline") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.InlineLocalisation)
        }
        
        if(expressionString == "abs_filepath") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AbsoluteFilePath)
        }
        
        if(expressionString == "filename") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.FileName)
        }
        expressionString.removeSurroundingOrNull("filename[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.FileName, value)
        }
        
        if(expressionString == "filepath") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.FilePath)
        }
        expressionString.removeSurroundingOrNull("filepath[", "]")?.let { v ->
            val value = v.removePrefix("game/").trim('/').orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.FilePath, value)
        }
        
        expressionString.removeSurroundingOrNull("icon[", "]")?.let { v ->
            val value = v.removePrefix("game/").trim('/').orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Icon, value)
        }
        
        if(expressionString == "<modifier>") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Modifier)
        }
        
        if(expressionString == "<technology_with_level>") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TechnologyWithLevel)
        }
        
        expressionString.removeSurroundingOrNull("<", ">")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Definition, value)
        }
        
        expressionString.removeSurroundingOrNull("value[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Value, value)
        }
        
        expressionString.removeSurroundingOrNull("value_set[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ValueSet, value)
        }
        
        expressionString.removeSurroundingOrNull("dynamic_value[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.DynamicValue, value)
        }
        
        expressionString.removeSurroundingOrNull("enum[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.EnumValue, value)
        }
        
        if(expressionString == "scope_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ScopeField)
        }
        
        expressionString.removeSurroundingOrNull("scope[", "]")?.let { v ->
            val value = v.orNull().takeIf { it != "any" }
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Scope, value)
        }
        
        expressionString.removeSurroundingOrNull("scope_group[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ScopeGroup, value)
        }
        
        if(expressionString == "value_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ValueField)
        }
        expressionString.removeSurroundingOrNull("value_field[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ValueField, value)
        }
        
        if(expressionString == "int_value_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.IntValueField)
        }
        expressionString.removeSurroundingOrNull("int_value_field[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.IntValueField, value)
        }
        
        if(expressionString == "variable_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.VariableField)
        }
        if(expressionString == "variable_field_32") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.VariableField)
        }
        expressionString.removeSurroundingOrNull("variable_field[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.VariableField, value)
        }
        
        if(expressionString == "int_variable_field") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.IntVariableField)
        }
        if(expressionString == "int_variable_field_32") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.IntVariableField)
        }
        expressionString.removeSurroundingOrNull("int_variable_field[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.IntVariableField, value)
        }
        
        expressionString.removeSurroundingOrNull("single_alias_right[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SingleAliasRight, value)
        }
        
        expressionString.removeSurroundingOrNull("alias_name[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AliasName, value)
        }
        
        expressionString.removeSurroundingOrNull("alias_match_left[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AliasMatchLeft, value)
        }
        
        expressionString.removeSurroundingOrNull("alias_keys_field[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.AliasKeysField, value)
        }
        
        if(expressionString == "\$any") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Any)
        }
        
        if(expressionString == "\$parameter") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Parameter)
        }
        
        if(expressionString == "\$parameter_value") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ParameterValue)
        }
        
        if(expressionString == "\$localisation_parameter") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.LocalisationParameter)
        }
        
        if(expressionString == "\$shader_effect") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.ShaderEffect)
        }
        
        if(expressionString == "\$database_object") {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.DatabaseObject)
        }
        
        expressionString.removeSurroundingOrNull("stellaris_name_format[", "]")?.let { v ->
            val value = v.orNull()
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.StellarisNameFormat, value)
        }
        
        return null
    }
}

class ConstantCwtDataExpressionResolver : CwtDataExpressionResolver, CwtConfigPatternAware {
    private val excludeCharacters = "$[]<>".toCharArray()
    
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if(expressionString.none { c -> c in excludeCharacters }) {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Constant, expressionString)
        }
        return null
    }
}

class TemplateExpressionCwtDataExpressionResolver : CwtDataExpressionResolver, CwtConfigPatternAware {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if(CwtTemplateExpression.resolve(expressionString).expressionString.isNotEmpty()) {
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TemplateExpression)
        }
        return null
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
