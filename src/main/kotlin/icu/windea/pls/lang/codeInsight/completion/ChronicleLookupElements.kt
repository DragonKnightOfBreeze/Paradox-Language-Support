package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import icu.windea.pls.model.constants.PlsStrings

object ChronicleLookupElements {
    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(ChronicleCompletionPriorities.keyword)
        .withCompletionId()
    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(ChronicleCompletionPriorities.keyword)
        .withCompletionId()
    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText(PlsStrings.blockFolder)
        .withPriority(ChronicleCompletionPriorities.keyword)
        .withCompletionId(PlsStrings.blockFolder)
        .withInsertHandler(ChronicleInsertHandlers.block())

    val keywordLookupElements = listOf(yesLookupElement, noLookupElement, blockLookupElement)

    val cardinalityElements = listOf("0..1", "1..1", "0..inf", "1..inf").map {
        LookupElementBuilder.create(it)
            .withPriority(ChronicleCompletionPriorities.constant)
            .withCompletionId()
    }
}
