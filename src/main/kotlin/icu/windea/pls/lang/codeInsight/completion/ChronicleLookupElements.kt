package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import icu.windea.pls.model.constants.ChronicleStrings

object ChronicleLookupElements {
    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(ChronicleCompletionPriorities.keyword)
        .withCompletionId()
    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(ChronicleCompletionPriorities.keyword)
        .withCompletionId()
    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText(ChronicleStrings.blockFolder)
        .withPriority(ChronicleCompletionPriorities.keyword)
        .withCompletionId(ChronicleStrings.blockFolder)
        .withInsertHandler(ChronicleInsertHandlers.block())

    val keywordLookupElements = listOf(yesLookupElement, noLookupElement, blockLookupElement)
}
