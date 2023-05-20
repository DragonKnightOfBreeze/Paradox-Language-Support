package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.*

/**
 * CWT键表达式。
 */
class CwtKeyExpression private constructor(
    expressionString: String,
    override val type: CwtDataType,
    override val value: String? = null,
    override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtDataExpression {
    companion object Resolver {
        val EmptyStringExpression = CwtKeyExpression("", CwtDataType.Constant, "")
        
        val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtKeyExpression> { doResolve(it) } }
        
        fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
        
        private fun doResolve(expressionString: String) = when {
            expressionString.isEmpty() -> EmptyStringExpression
            expressionString == "bool" -> {
                CwtKeyExpression(expressionString, CwtDataType.Bool)
            }
            expressionString == "int" -> {
                CwtKeyExpression(expressionString, CwtDataType.Int)
            }
            expressionString.surroundsWith("int[", "]") -> {
                val range = expressionString.substring(4, expressionString.length - 1)
                    .split("..", limit = 2)
                    .let { tupleOf(it.getOrNull(0)?.toIntOrNull(), it.getOrNull(1)?.toIntOrNull()) }
                CwtKeyExpression(expressionString, CwtDataType.Int, null, range)
            }
            expressionString == "float" -> {
                CwtKeyExpression(expressionString, CwtDataType.Float)
            }
            expressionString.surroundsWith("float[", "]") -> {
                val range = expressionString.substring(6, expressionString.length - 1)
                    .split("..", limit = 2)
                    .let { tupleOf(it.getOrNull(0)?.toIntOrNull(), it.getOrNull(1)?.toIntOrNull()) }
                CwtKeyExpression(expressionString, CwtDataType.Float, null, range)
            }
            expressionString == "scalar" -> {
                CwtKeyExpression(expressionString, CwtDataType.Scalar)
            }
            expressionString == "colour_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.ColorField)
            }
            expressionString == "percentage_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.PercentageField)
            }
            expressionString == "date_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.DateField)
            }
            expressionString == "localisation" -> {
                CwtKeyExpression(expressionString, CwtDataType.Localisation)
            }
            expressionString == "localisation_synced" -> {
                CwtKeyExpression(expressionString, CwtDataType.SyncedLocalisation)
            }
            expressionString == "localisation_inline" -> {
                CwtKeyExpression(expressionString, CwtDataType.InlineLocalisation)
            }
            //EXTENDED BY PLS
            expressionString == "abs_filepath" -> {
                CwtKeyExpression(expressionString, CwtDataType.AbsoluteFilePath)
            }
            //EXTENDED BY PLS
            expressionString == "filename" -> {
                CwtKeyExpression(expressionString, CwtDataType.FileName)
            }
            //EXTENDED BY PLS
            expressionString.surroundsWith("filename[", "]") -> {
                val value = expressionString.substring(9, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.FileName, value)
            }
            expressionString == "filepath" -> {
                CwtKeyExpression(expressionString, CwtDataType.FilePath)
            }
            expressionString.surroundsWith("filepath[", "]") -> {
                val value = expressionString.substring(9, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.FilePath, value)
            }
            expressionString.surroundsWith("icon[", "]") -> {
                val value = expressionString.substring(5, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.Icon, value)
            }
            expressionString == "<modifier>" -> {
                CwtKeyExpression(expressionString, CwtDataType.Modifier)
            }
            expressionString.surroundsWith('<', '>') -> {
                val value = expressionString.substring(1, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.Definition, value)
            }
            //EXTENDED BY PLS
            expressionString == "\$parameter" -> {
                CwtKeyExpression("parameter", CwtDataType.Parameter)
            }
            //EXTENDED BY PLS
            expressionString == "\$parameter_value" -> {
                CwtKeyExpression("parameter_value", CwtDataType.ParameterValue)
            }
            //EXTENDED BY PLS
            expressionString == "\$localisation_parameter" -> {
                CwtKeyExpression("localisation_parameter", CwtDataType.LocalisationParameter)
            }
            expressionString.surroundsWith("value[", "]") -> {
                val value = expressionString.substring(6, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.Value, value)
            }
            expressionString.surroundsWith("value_set[", "]") -> {
                val value = expressionString.substring(10, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.ValueSet, value)
            }
            expressionString.surroundsWith("enum[", "]") -> {
                val value = expressionString.substring(5, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.EnumValue, value)
            }
            expressionString == "scope_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.ScopeField)
            }
            expressionString.surroundsWith("scope[", "]") -> {
                //value需要是有效的scope_type
                val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
                CwtKeyExpression(expressionString, CwtDataType.Scope, value)
            }
            expressionString.surroundsWith("scope_group[", "]") -> {
                val value = expressionString.substring(12, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.ScopeGroup, value)
            }
            expressionString == "value_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.ValueField)
            }
            expressionString.surroundsWith("value_field[", "]") -> {
                val value = expressionString.substring(12, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.ValueField, value)
            }
            expressionString == "int_value_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.IntValueField)
            }
            expressionString.surroundsWith("int_value_field[", "]") -> {
                val value = expressionString.substring(16, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.IntValueField, value)
            }
            expressionString == "variable_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.VariableField)
            }
            expressionString == "variable_field_32" -> {
                CwtKeyExpression(expressionString, CwtDataType.VariableField)
            }
            expressionString.surroundsWith("variable_field[", "]") -> {
                val value = expressionString.substring(15, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.VariableField, value)
            }
            expressionString == "int_variable_field" -> {
                CwtKeyExpression(expressionString, CwtDataType.IntVariableField)
            }
            expressionString == "int_variable_field_32" -> {
                CwtKeyExpression(expressionString, CwtDataType.IntVariableField)
            }
            expressionString.surroundsWith("int_variable_field[", "]") -> {
                val value = expressionString.substring(19, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.IntVariableField, value)
            }
            expressionString.surroundsWith("alias_keys_field[", "]") -> {
                val value = expressionString.substring(17, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.AliasKeysField, value)
            }
            expressionString.surroundsWith("alias_name[", "]") -> {
                val value = expressionString.substring(11, expressionString.length - 1)
                CwtKeyExpression(expressionString, CwtDataType.AliasName, value)
            }
            CwtTemplateExpression.resolve(expressionString).isNotEmpty() -> {
                CwtKeyExpression(expressionString, CwtDataType.Template)
            }
            expressionString == "\$any" -> {
                CwtKeyExpression("any", CwtDataType.Any)
            }
            expressionString.endsWith(']') -> {
                CwtKeyExpression(expressionString, CwtDataType.Other)
            }
            else -> {
                CwtKeyExpression(expressionString, CwtDataType.Constant, expressionString)
            }
        }
    }
    
    operator fun component1() = type
    
    operator fun component2() = value
}