package icu.windea.pls.cwt.expression

import icu.windea.pls.*

class CwtValueExpression(
	expression: String,
	val type: Type,
	val value: String? = null,
	val extraValue:Any? = null
) : AbstractExpression(expression) {
	companion object Resolver : AbstractExpressionResolver<CwtValueExpression>() {
		override val emptyExpression = CwtValueExpression("",Type.Constant,"")
		
		override fun resolve(expression: String): CwtValueExpression {
			return cache.getOrPut(expression) { doResolve(expression) }
		}
		
		private fun doResolve(expression: String): CwtValueExpression {
			return when {
				expression.isEmpty() -> {
					emptyExpression
				}
				expression == "any" -> {
					CwtValueExpression(expression, Type.Any)
				}
				expression == "bool" -> {
					CwtValueExpression(expression, Type.Bool)
				}
				expression == "int" -> {
					CwtValueExpression(expression, Type.Int)
				}
				expression.surroundsWith("int[", "]") -> {
					val extraValue = expression.substring(4, expression.length - 1).toIntRangeOrNull()
					CwtValueExpression(expression, Type.IntExpression, null,extraValue)
				}
				expression == "float" -> {
					CwtValueExpression(expression, Type.Float)
				}
				expression.surroundsWith("float[", "]") -> {
					val extraValue = expression.substring(6, expression.length - 1)
					CwtValueExpression(expression, Type.FloatExpression, null,extraValue)
				}
				expression == "scalar" -> {
					CwtValueExpression(expression, Type.Scalar)
				}
				expression == "percentage_field" -> {
					CwtValueExpression(expression, Type.PercentageField)
				}
				expression == "color_field" -> {
					CwtValueExpression(expression, Type.ColorField)
				}
				expression == "localisation" -> {
					CwtValueExpression(expression, Type.Localisation)
				}
				expression == "localisation_synced" -> {
					CwtValueExpression(expression, Type.SyncedLocalisation)
				}
				expression == "localisation_inline" -> {
					CwtValueExpression(expression, Type.InlineLocalisation)
				}
				expression == "filepath" -> {
					CwtValueExpression(expression, Type.FilePath)
				}
				expression.surroundsWith("filepath[", "]") -> {
					val value = expression.substring(9, expression.length - 1)
					CwtValueExpression(expression, Type.FilePathExpression, value)
				}
				expression.surroundsWith("icon[", "]") -> {
					val value = expression.substring(5, expression.length - 1)
					CwtValueExpression(expression, Type.IconExpression, value)
				}
				expression == "date_field" -> {
					CwtValueExpression(expression, Type.DateField)
				}
				expression.surroundsWith('<', '>') -> {
					val value = expression.substring(1, expression.length - 1)
					CwtValueExpression(expression, Type.TypeExpression, value)
				}
				expression.indexOf('<').let { it > 0 && it < expression.indexOf('>') } -> {
					val value = expression.substring(expression.indexOf('<'), expression.indexOf('>'))
					val extraValue = expression.substringBefore('<') to expression.substringAfterLast('>')
					CwtValueExpression(expression, Type.TypeExpressionString, value,extraValue)
				}
				expression.surroundsWith("value[", "]") -> {
					val value = expression.substring(6, expression.length - 1)
					CwtValueExpression(expression, Type.ValueExpression, value)
				}
				expression.surroundsWith("enum[", "]") -> {
					val value = expression.substring(5, expression.length - 1)
					CwtValueExpression(expression, Type.EnumExpression, value)
				}
				expression.surroundsWith("scope[", "]") -> {
					val value = expression.substring(6, expression.length - 1)
					CwtValueExpression(expression, Type.ScopeExpression, value)
				}
				expression == "scope_field" -> {
					CwtValueExpression(expression, Type.ScopeField)
				}
				expression == "variable_field" -> {
					CwtValueExpression(expression, Type.VariableField)
				}
				expression.surroundsWith("variable_field[", "]") -> {
					val value = expression.substring(15, expression.length - 1)
					CwtValueExpression(expression, Type.VariableFieldExpression, value)
				}
				expression == "int_variable_field" -> {
					CwtValueExpression(expression, Type.IntVariableField)
				}
				expression.surroundsWith("int_variable_field[", "]") -> {
					val value = expression.substring(19, expression.length - 1)
					CwtValueExpression(expression, Type.IntVariableFieldExpression, value)
				}
				expression == "value_field" -> {
					CwtValueExpression(expression, Type.ValueField)
				}
				expression.surroundsWith("value_field[", "]") -> {
					val value = expression.substring(12, expression.length - 1)
					CwtValueExpression(expression, Type.ValueFieldExpression, value)
				}
				expression == "int_value_field" -> {
					CwtValueExpression(expression, Type.IntValueField)
				}
				expression.surroundsWith("int_value_field[", "]") -> {
					val value = expression.substring(16, expression.length - 1)
					CwtValueExpression(expression, Type.IntValueFieldExpression, value)
				}
				expression.surroundsWith("alias_match_left[", "]") -> {
					val value = expression.substring(17, expression.length - 1)
					CwtValueExpression(expression, Type.AliasMatchLeftExpression, value)
				}
				else -> {
					val value = expression
					CwtValueExpression(expression, Type.Constant, value)
				}
			}
		}
	}
	
	enum class Type {
		Any,
		Bool,
		Int,
		IntExpression,
		Float,
		FloatExpression,
		Scalar,
		PercentageField,
		ColorField,
		Localisation,
		SyncedLocalisation,
		InlineLocalisation,
		FilePath,
		FilePathExpression,
		IconExpression,
		DateField,
		TypeExpression,
		TypeExpressionString,
		ValueExpression,
		EnumExpression,
		ScopeExpression,
		ScopeField,
		VariableField,
		VariableFieldExpression,
		IntVariableField,
		IntVariableFieldExpression,
		ValueField,
		ValueFieldExpression,
		IntValueField,
		IntValueFieldExpression,
		AliasMatchLeftExpression,
		Constant
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}