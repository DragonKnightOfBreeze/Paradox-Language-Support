package icu.windea.pls.config.cwt.expression

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*

enum class CwtDataType {
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
	AbsoluteFilePath, //EXTENDED BY PLS
	FilePath,
	FileName, //EXTENDED BY PLS
	Icon,
	Definition,
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
	Modifier, //<modifier>
	SingleAliasRight,
	AliasName,
	AliasKeysField,
	AliasMatchLeft,
	TemplateExpression,
	Constant,
	Other,
	@WithGameType(ParadoxGameType.Stellaris)
	StellarisNameFormat;
	
	//modify implementation of below methods should also check codes that directly based on enum constants
	//so they are just as a convenience
	
	fun isNumberType() = this == Int || this == Float
		|| this == ValueField || this == IntValueField
		|| this == VariableField || this == IntVariableField
	
	fun isFilePathType() = this == AbsoluteFilePath
		|| this == FileName || this == FilePath || this == Icon
	
	fun isScopeFieldType() = this == ScopeField || this == Scope || this == ScopeGroup
		
	fun isValueFieldType() = this == ValueField || this == IntValueField
	
	fun isVariableFieldType() = this == VariableField || this == IntVariableField
	
	fun isValueSetValueType() = this == Value || this == ValueSet
	
	fun isGeneratorType() = this == Constant || this == TemplateExpression
}