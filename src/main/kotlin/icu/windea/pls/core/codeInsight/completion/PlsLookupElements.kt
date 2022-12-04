package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.lookup.*
import icu.windea.pls.core.*

object PlsLookupElements {
	val yesLookupElement = LookupElementBuilder.create("yes").bold().withPriority(PlsCompletionPriorities.keywordPriority)
	val noLookupElement = LookupElementBuilder.create("no").bold().withPriority(PlsCompletionPriorities.keywordPriority)
}