package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.*

class PlsLookupElement(
    val delegate: LookupElement,
    val extraElements: List<LookupElement> = emptyList()
) : LookupElementDecorator<LookupElement>(delegate)