package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.*

class PlsLookupElement(
    delegate: LookupElement,
    val extraElements: List<LookupElement> = emptyList()
) : LookupElementDecorator<LookupElement>(delegate)