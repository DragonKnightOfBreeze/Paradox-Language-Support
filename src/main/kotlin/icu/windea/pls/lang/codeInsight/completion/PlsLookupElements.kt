package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder

object PlsLookupElements {
    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(PlsCompletionPriorities.keyword)
        .withCompletionId()
    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(PlsCompletionPriorities.keyword)
        .withCompletionId()
    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText("{...}")
        .withPriority(PlsCompletionPriorities.keyword)
        .withCompletionId("{...}")
        .withInsertHandler(PlsInsertHandlers.block())

    val keywordLookupElements = listOf(yesLookupElement, noLookupElement, blockLookupElement)
}
