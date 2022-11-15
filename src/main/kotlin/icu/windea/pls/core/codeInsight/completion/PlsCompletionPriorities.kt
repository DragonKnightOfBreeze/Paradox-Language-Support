package icu.windea.pls.core.codeInsight.completion

object PlsCompletionPriorities {
	const val pinnedPriority = 1000.0
	const val keywordPriority = 100.0
	const val constantPriority = 90.0
	const val scopeFieldPrefixPriority = 95.0
	const val valueFieldPrefixPriority = 95.0
	const val modifierPriority = 80.0
	const val systemScopePriority = 70.0
	const val scopePriority = 70.0
	const val localisationCommandPriority = 50.0
	//const val pathPriority = 60.0
	//const val definitionPriority = 50.0
	const val enumPriority = 85.0
	//const val complexEnumPriority = 40.0
	//const val valueFieldValuePriority = 30.0
	const val predefinedValueSetValuePriority = 80.0
	//const val valueSetValuePriority = 20.0
	//const val variablePriority = 15.0
	
	const val scopeMismatchOffset = -500 
}
