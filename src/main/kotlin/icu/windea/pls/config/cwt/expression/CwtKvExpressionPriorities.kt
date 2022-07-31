package icu.windea.pls.config.cwt.expression

object CwtKvExpressionPriorities {
	const val constantPriority = 300
	const val rangedConstantPriority = 301
	const val enumPriority = 200
	const val modifierPriority = 190
	const val variableFieldPriority = 180
	const val rangedVariableFieldPriority = 181
	const val aliasPriority = 110
	const val fileReferencePriority = 100
	const val rangedFileReferencePriority = 101
	const val definitionReferencePriority = 90
	const val localisationReferencePriority = 80
	const val syncedLocalisationReferencePriority = 70
	const val complexEnumPriority = 60
	const val valuePriority = 50
	const val scopeFieldPriority = 40
	const val rangedScopeFieldPriority = 41
	const val valueFieldPriority = 30
	const val rangedValueFieldPriority = 31
	const val parametersPriority = 1
	const val scalarPriority = 0
	const val fallbackPriority = -1
}