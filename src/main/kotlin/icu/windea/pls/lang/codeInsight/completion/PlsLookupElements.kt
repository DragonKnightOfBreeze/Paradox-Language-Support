package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import icu.windea.pls.model.constants.PlsStrings

object PlsLookupElements {
    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(PlsCompletionPriorities.keyword)
        .withCompletionId()
    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(PlsCompletionPriorities.keyword)
        .withCompletionId()
    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText(PlsStrings.blockFolder)
        .withPriority(PlsCompletionPriorities.keyword)
        .withCompletionId(PlsStrings.blockFolder)
        .withInsertHandler(PlsInsertHandlers.block())

    val keywordLookupElements = listOf(yesLookupElement, noLookupElement, blockLookupElement)
}
