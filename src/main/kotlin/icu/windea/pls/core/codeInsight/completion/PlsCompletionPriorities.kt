package icu.windea.pls.core.codeInsight.completion

object PlsCompletionPriorities {
	const val pinnedPriority = 1000.0
	const val keywordPriority = 100.0
	const val scopeFieldPrefixPriority = 90.0
	const val valueFieldPrefixPriority = 85.0
	const val systemScopePriority = 65.0
	const val scopePriority = 60.0
	const val valueOfValueFieldPriority = 55.0
	const val localisationCommandPriority = 50.0
	const val propertyPriority = 40.0
	const val valuePriority = 40.0
	const val modifierPriority = 20.0
	
	const val scopeMismatchOffset = -500 
}