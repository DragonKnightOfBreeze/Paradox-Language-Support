package icu.windea.pls.config.cwt.expression

sealed interface CwtDataType

sealed interface CwtKeyDataType : CwtDataType

sealed interface CwtValueDataType : CwtDataType

object CwtDataTypes {
	object Any : CwtKeyDataType, CwtValueDataType
	object Bool : CwtValueDataType
	object Int : CwtKeyDataType, CwtValueDataType
	object Float : CwtKeyDataType, CwtValueDataType
	object Scalar : CwtKeyDataType, CwtValueDataType
	object ColorField : CwtValueDataType
	object PercentageField : CwtValueDataType
	object DateField : CwtValueDataType
	object Localisation : CwtKeyDataType, CwtValueDataType
	object SyncedLocalisation : CwtKeyDataType, CwtValueDataType
	object InlineLocalisation : CwtKeyDataType, CwtValueDataType
	object AbsoluteFilePath : CwtValueDataType //EXTENDED BY PLS
	object FilePath : CwtValueDataType
	object Icon : CwtValueDataType
	object TypeExpression : CwtKeyDataType, CwtValueDataType
	object TypeExpressionString : CwtKeyDataType, CwtValueDataType
	object Value : CwtKeyDataType, CwtValueDataType
	object ValueSet : CwtKeyDataType, CwtValueDataType
	object Enum : CwtKeyDataType, CwtValueDataType
	object ComplexEnum : CwtKeyDataType, CwtValueDataType
	object ScopeGroup : CwtValueDataType
	object Scope : CwtKeyDataType, CwtValueDataType
	object VariableField : CwtValueDataType
	object IntVariableField : CwtValueDataType
	object ValueField : CwtValueDataType
	object IntValueField : CwtValueDataType
	object SingleAliasRight : CwtValueDataType
	object AliasName : CwtKeyDataType
	object AliasKeysField : CwtKeyDataType, CwtValueDataType
	object AliasMatchLeft : CwtValueDataType
	object Constant : CwtKeyDataType, CwtValueDataType
	object Other : CwtKeyDataType, CwtValueDataType
}