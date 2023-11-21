package icu.windea.pls.lang.expression

import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.expression.CwtDataExpressionResolver.*

class BaseCwtDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String): Result? {
        return when {
            expressionString == "bool" -> {
                Result(expressionString, CwtDataTypes.Bool)
            }
            expressionString == "int" -> {
                Result(expressionString, CwtDataTypes.Int)
            }
            expressionString.surroundsWith("int[", "]") -> {
                val range = expressionString.substring(4, expressionString.length - 1)
                    .split("..", limit = 2)
                    .let { tupleOf(it.getOrNull(0)?.toIntOrNull(), it.getOrNull(1)?.toIntOrNull()) }
                Result(expressionString, CwtDataTypes.Int, null, range)
            }
            expressionString == "float" -> {
                Result(expressionString, CwtDataTypes.Float)
            }
            expressionString.surroundsWith("float[", "]") -> {
                val range = expressionString.substring(6, expressionString.length - 1)
                    .split("..", limit = 2)
                    .let { tupleOf(it.getOrNull(0)?.toFloatOrNull(), it.getOrNull(1)?.toFloatOrNull()) }
                Result(expressionString, CwtDataTypes.Float, null, range)
            }
            expressionString == "scalar" -> {
                Result(expressionString, CwtDataTypes.Scalar)
            }
            expressionString == "colour_field" -> {
                Result(expressionString, CwtDataTypes.ColorField)
            }
            expressionString.surroundsWith("colour[", "]") -> {
                val value = expressionString.substring(7, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.ColorField, value)
            }
            else -> null
        }
    }
}

class CoreCwtDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String): Result? {
        return when {
            expressionString == "percentage_field" -> {
                Result(expressionString, CwtDataTypes.PercentageField)
            }
            expressionString == "date_field" -> {
                Result(expressionString, CwtDataTypes.DateField)
            }
            expressionString == "localisation" -> {
                Result(expressionString, CwtDataTypes.Localisation)
            }
            expressionString == "localisation_synced" -> {
                Result(expressionString, CwtDataTypes.SyncedLocalisation)
            }
            expressionString == "localisation_inline" -> {
                Result(expressionString, CwtDataTypes.InlineLocalisation)
            }
            expressionString == "abs_filepath" -> {
                Result(expressionString, CwtDataTypes.AbsoluteFilePath)
            }
            expressionString == "filename" -> {
                Result(expressionString, CwtDataTypes.FileName)
            }
            expressionString.surroundsWith("filename[", "]") -> {
                val value = expressionString.substring(9, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.FileName, value)
            }
            expressionString == "filepath" -> {
                Result(expressionString, CwtDataTypes.FilePath)
            }
            expressionString.surroundsWith("filepath[", "]") -> {
                val value = expressionString.substring(9, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.FilePath, value)
            }
            expressionString.surroundsWith("icon[", "]") -> {
                val value = expressionString.substring(5, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.Icon, value)
            }
            expressionString == "<modifier>" -> {
                Result(expressionString, CwtDataTypes.Modifier)
            }
            expressionString.surroundsWith('<', '>') -> {
                val value = expressionString.substring(1, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.Definition, value)
            }
            expressionString.surroundsWith("value[", "]") -> {
                val value = expressionString.substring(6, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.Value, value)
            }
            expressionString.surroundsWith("value_set[", "]") -> {
                val value = expressionString.substring(10, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.ValueSet, value)
            }
            expressionString.surroundsWith("value_or_value_set[", "]") -> {
                val value = expressionString.substring(19, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.ValueOrValueSet, value)
            }
            expressionString.surroundsWith("enum[", "]") -> {
                val value = expressionString.substring(5, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.EnumValue, value)
            }
            expressionString == "scope_field" -> {
                Result(expressionString, CwtDataTypes.ScopeField)
            }
            expressionString.surroundsWith("scope[", "]") -> {
                //value需要是有效的scope_type
                val value = expressionString.substring(6, expressionString.length - 1).orNull().takeIf { it != "any" }
                Result(expressionString, CwtDataTypes.Scope, value)
            }
            expressionString.surroundsWith("scope_group[", "]") -> {
                val value = expressionString.substring(12, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.ScopeGroup, value)
            }
            expressionString == "value_field" -> {
                Result(expressionString, CwtDataTypes.ValueField)
            }
            expressionString.surroundsWith("value_field[", "]") -> {
                val value = expressionString.substring(12, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.ValueField, value)
            }
            expressionString == "int_value_field" -> {
                Result(expressionString, CwtDataTypes.IntValueField)
            }
            expressionString.surroundsWith("int_value_field[", "]") -> {
                val value = expressionString.substring(16, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.IntValueField, value)
            }
            expressionString == "variable_field" -> {
                Result(expressionString, CwtDataTypes.VariableField)
            }
            expressionString == "variable_field_32" -> {
                Result(expressionString, CwtDataTypes.VariableField)
            }
            expressionString.surroundsWith("variable_field[", "]") -> {
                val value = expressionString.substring(15, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.VariableField, value)
            }
            expressionString == "int_variable_field" -> {
                Result(expressionString, CwtDataTypes.IntVariableField)
            }
            expressionString == "int_variable_field_32" -> {
                Result(expressionString, CwtDataTypes.IntVariableField)
            }
            expressionString.surroundsWith("int_variable_field[", "]") -> {
                val value = expressionString.substring(19, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.IntVariableField, value)
            }
            expressionString.surroundsWith("single_alias_right[", "]") -> {
                val value = expressionString.substring(19, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.SingleAliasRight, value)
            }
            expressionString.surroundsWith("alias_name[", "]") -> {
                val value = expressionString.substring(11, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.AliasName, value)
            }
            expressionString.surroundsWith("alias_match_left[", "]") -> {
                val value = expressionString.substring(17, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.AliasMatchLeft, value)
            }
            expressionString.surroundsWith("alias_keys_field[", "]") -> {
                val value = expressionString.substring(17, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.AliasKeysField, value)
            }
            else -> null
        }
    }
}

class ExtendedCwtDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String): Result? {
        return when {
            expressionString == "\$any" -> {
                Result("any", CwtDataTypes.Any)
            }
            expressionString == "\$parameter" -> {
                Result("parameter", CwtDataTypes.Parameter)
            }
            expressionString == "\$parameter_value" -> {
                Result("parameter_value", CwtDataTypes.ParameterValue)
            }
            expressionString == "\$localisation_parameter" -> {
                Result("localisation_parameter", CwtDataTypes.LocalisationParameter)
            }
            expressionString == "\$shader_effect" -> {
                Result("shader_effect", CwtDataTypes.ShaderEffect)
            }
            expressionString.surroundsWith("\$stellaris_name_format[", "]") -> {
                val value = expressionString.substring(22, expressionString.length - 1).orNull()
                Result(expressionString, CwtDataTypes.StellarisNameFormat, value)
            }
            expressionString == "\$technology_with_level" -> {
                Result("technology_with_level", CwtDataTypes.TechnologyWithLevel)
            }
            else -> null
        }
    }
}

class ConstantCwtDataExpressionResolver : CwtDataExpressionResolver {
    private val excludeCharacters = "$[]<>".toCharArray()
    
    override fun resolve(expressionString: String): Result? {
        if(expressionString.none { c -> c in excludeCharacters }) return Result(expressionString, CwtDataTypes.Constant, expressionString)
        return null
    }
}

class TemplateCwtDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String): Result? {
        if(CwtTemplateExpression.resolve(expressionString).isNotEmpty()) return Result(expressionString, CwtDataTypes.Template)
        return null
    }
}