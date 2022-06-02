package icu.windea.pls.config.cwt.expression

sealed interface CwtKvExpressionType

sealed interface CwtKeyExpressionType : CwtKvExpressionType

sealed interface CwtValueExpressionType : CwtKvExpressionType

object CwtKvExpressionTypes {
	object Any : CwtKeyExpressionType, CwtValueExpressionType
	object Bool : CwtKeyExpressionType, CwtValueExpressionType
	object Int : CwtKeyExpressionType, CwtValueExpressionType
	object IntRange : CwtKeyExpressionType, CwtValueExpressionType
	object Float : CwtKeyExpressionType, CwtValueExpressionType
	object FloatRange : CwtKeyExpressionType, CwtValueExpressionType
	object Scalar : CwtKeyExpressionType, CwtValueExpressionType
	object PercentageField : CwtValueExpressionType
	object ColorField : CwtValueExpressionType
	object DateField : CwtValueExpressionType
	object Localisation : CwtKeyExpressionType, CwtValueExpressionType
	object SyncedLocalisation : CwtKeyExpressionType, CwtValueExpressionType
	object InlineLocalisation : CwtKeyExpressionType, CwtValueExpressionType
	object AbsoluteFilePath : CwtValueExpressionType //EXTENDED BY PLS
	object FilePath : CwtValueExpressionType
	object Icon : CwtValueExpressionType
	object TypeExpression : CwtKeyExpressionType, CwtValueExpressionType
	object TypeExpressionString : CwtKeyExpressionType, CwtValueExpressionType
	object Value : CwtKeyExpressionType, CwtValueExpressionType
	object ValueSet : CwtKeyExpressionType, CwtValueExpressionType
	object Enum : CwtKeyExpressionType, CwtValueExpressionType
	object ComplexEnum : CwtKeyExpressionType, CwtValueExpressionType
	object Scope : CwtKeyExpressionType, CwtValueExpressionType
	object ScopeField : CwtKeyExpressionType, CwtValueExpressionType
	object VariableField : CwtValueExpressionType
	object IntVariableField : CwtValueExpressionType
	object ValueField : CwtValueExpressionType
	object IntValueField : CwtValueExpressionType
	object SingleAliasRight : CwtValueExpressionType
	object AliasName : CwtKeyExpressionType
	object AliasKeysField : CwtKeyExpressionType, CwtValueExpressionType
	object AliasMatchLeft : CwtValueExpressionType
	object Constant : CwtKeyExpressionType, CwtValueExpressionType
	object Other : CwtKeyExpressionType, CwtValueExpressionType
}