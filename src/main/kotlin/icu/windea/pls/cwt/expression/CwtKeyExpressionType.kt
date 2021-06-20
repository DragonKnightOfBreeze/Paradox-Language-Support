package icu.windea.pls.cwt.expression

enum class CwtKeyExpressionType: CwtExpressionType {
	Any,
	Bool,
	Int,
	IntExpression,
	Float,
	FloatExpression,
	Scalar,
	Localisation,
	SyncedLocalisation,
	InlineLocalisation,
	TypeExpression,
	TypeExpressionString,
	EnumExpression,
	ScopeExpression,
	AliasNameExpression,
	Constant
}