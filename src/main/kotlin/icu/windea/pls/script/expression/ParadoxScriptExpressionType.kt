package icu.windea.pls.script.expression

import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.ParadoxExpressionType.*

fun ParadoxExpressionType.isBooleanType() = this == BooleanType

fun ParadoxExpressionType.isIntType() = this == UnknownType || this == IntType || this == ParameterType || this == InlineMathType

fun ParadoxExpressionType.isFloatType() = this == UnknownType || this == IntType || this == FloatType || this == ParameterType || this == InlineMathType

fun ParadoxExpressionType.isStringType() = this == UnknownType || this == StringType || this == ParameterType

fun ParadoxExpressionType.isColorType() = this == ColorType

fun ParadoxExpressionType.canBeScriptedVariableValue() = this == BooleanType || this == IntType || this == FloatType