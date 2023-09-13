package icu.windea.pls.core.codeInsight.completion

object PlsCompletionPriorities {
	const val pinnedPriority = 1000.0
	const val keywordPriority = 10.0
	const val rootKeyPriority = 90.0
	const val constantPriority = 90.0
	const val enumPriority = 88.0
	const val complexEnumPriority = 80.0
	const val scopeLinkPrefixPriority = 70.0
	const val valueLinkPrefixPriority = 70.0
	const val systemScopePriority = 60.0
	const val scopePriority = 60.0
	
	const val scopeMismatchOffset = -500 
}
