package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.*

/**
 * 提供布尔值常量的代码补全。始终提示。
 */
object BooleanCompletionProvider : CompletionProvider<CompletionParameters>() {
	private val booleanLookupElements = booleanValues.map { value ->
		LookupElementBuilder.create(value).bold().withPriority(keywordPriority)
	}
	
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		result.addAllElements(booleanLookupElements) //总是提示
	}
}