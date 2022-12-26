package icu.windea.pls.config.cwt.expression

import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.model.*

enum class CwtDataType {
	Any,
	Bool,
	Int,
	Float,
	Scalar,
	ColorField,
	PercentageField,
	DateField,
	Localisation,
	SyncedLocalisation,
	InlineLocalisation,
	//EXTENDED BY PLS
	AbsoluteFilePath,
	FilePath,
	Icon,
	TypeExpression,
	TypeExpressionString,
	Enum,
	Value,
	ValueSet,
	ScopeField,
	Scope,
	ScopeGroup,
	ValueField,
	IntValueField,
	VariableField,
	IntVariableField,
	//EXTENDED BY PLS
	Modifier,
	SingleAliasRight,
	AliasName,
	AliasKeysField,
	AliasMatchLeft,
	ConstantKey,
	Constant,
	Other,
	@WithGameType(ParadoxGameType.Stellaris)
	StellarisNameFormat;
	
	fun CwtDataType.isConstant() = this == CwtDataType.ConstantKey || this == CwtDataType.Constant
	
	fun CwtDataType.isNumberType() = this == CwtDataType.Int || this == CwtDataType.Float
		|| this == CwtDataType.ValueField || this == CwtDataType.IntValueField
		|| this == CwtDataType.VariableField || this == CwtDataType.IntVariableField
}