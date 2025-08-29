package icu.windea.pls.core.codeInsight

import com.intellij.codeInsight.lookup.LookupElement

class CompositeLookupElement(
    val element: LookupElement,
    val extraElements: List<LookupElement> = emptyList()
)
