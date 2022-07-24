package icu.windea.pls.config.cwt.expression

object CwtKvExpressionPriorities {
	const val constantPriority = 300
	const val rangedConstantPriority = 301
	const val enumPriority = 200
	const val scopePriority = 190
	const val rangedScopePriority = 191
	const val variableFieldPriority = 180
	const val rangedVariableFieldPriority = 181
	const val valueFieldPriority = 170
	const val rangedValueFieldPriority = 171
	const val aliasPriority = 110
	const val fileReferencePriority = 100
	const val rangedFileReferencePriority = 101
	const val definitionReferencePriority = 90
	const val localisationReferencePriority = 80
	const val syncedLocalisationReferencePriority = 70
	const val complexEnumPriority = 60
	const val valuePriority = 50
	const val scalarPriority = 0
	const val fallbackPriority = -1
}