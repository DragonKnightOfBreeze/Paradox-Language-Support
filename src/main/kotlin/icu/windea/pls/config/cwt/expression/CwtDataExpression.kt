package icu.windea.pls.config.cwt.expression

import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*

/**
 * @property type 表达式类型，即CWT规则的dataType。
 */
sealed interface CwtDataExpression : CwtExpression {
	val type: CwtDataType
	val value: String?
	val extraValue: Any?
}

inline fun <reified T> CwtDataExpression.extraValue() = extraValue?.cast<T>()

fun CwtDataExpression.isNumberType(): Boolean {
	return type == CwtDataTypes.Int || type == CwtDataTypes.Float
		|| type == CwtDataTypes.ValueField || type == CwtDataTypes.IntValueField
		|| type == CwtDataTypes.VariableField || type == CwtDataTypes.VariableField
}

fun CwtDataExpression.resolved(definitionMemberInfo: ParadoxDefinitionMemberInfo?): CwtDataExpression {
	return CwtConfigExpressionResolver.resolved(this, definitionMemberInfo)
}