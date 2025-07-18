package icu.windea.pls.lang.references

import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.*

@Suppress("UnstableApiUsage")
abstract class ParadoxSymbolReferenceBase(
    private val element: PsiElement,
    private val rangeInElement: TextRange? = null,
) : PsiSymbolReference {
    override fun getElement(): PsiElement {
        return element
    }

    override fun getRangeInElement(): TextRange {
        return rangeInElement ?: TextRange(0, element.textLength)
    }
}
