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
	TemplateExpression,
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
	
	fun isConstant() = this == ConstantKey || this == Constant
	
	fun isNumberType() = this == Int || this == Float
		|| this == ValueField || this == IntValueField
		|| this == VariableField || this == IntVariableField
	
	fun isScopeFieldType() = this == ScopeField || this == Scope || this == ScopeGroup
		
	fun isValueFieldType() = this == ValueField || this == IntValueField
	
	fun isValueSetValueType() = this == Value || this == ValueSet
}