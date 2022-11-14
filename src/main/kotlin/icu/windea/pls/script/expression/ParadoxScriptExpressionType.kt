package icu.windea.pls.script.expression

import icu.windea.pls.script.exp.*
import icu.windea.pls.script.exp.ParadoxDataType.*

fun ParadoxDataType.isBooleanType() = this == BooleanType

fun ParadoxDataType.isIntType() = this == UnknownType || this == IntType || this == ParameterType || this == InlineMathType

fun ParadoxDataType.isFloatType() = this == UnknownType || this == IntType || this == FloatType || this == ParameterType || this == InlineMathType

fun ParadoxDataType.isStringType() = this == UnknownType || this == StringType || this == ParameterType

fun ParadoxDataType.isColorType() = this == ColorType

fun ParadoxDataType.canBeScriptedVariableValue() = this == BooleanType || this == IntType || this == FloatType