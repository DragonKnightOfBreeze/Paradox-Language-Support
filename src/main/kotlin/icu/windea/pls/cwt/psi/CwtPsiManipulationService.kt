package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.cwt.text.Cwt
import com.intellij.psi.ElementManipulator

/**
 * @see ElementManipulator
 */
object CwtPsiManipulationService {
    fun changeContent(element: CwtOptionKey, newContent: String, range: TextRange? = null): CwtOptionKey {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue.quoteIfNeeded(QuotePatterns.Cwt)
        val newElement = CwtElementFactory.createOptionKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtPropertyKey, newContent: String, range: TextRange? = null): CwtPropertyKey {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue.quoteIfNeeded(QuotePatterns.Cwt)
        val newElement = CwtElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtValue, newContent: String, range: TextRange? = null): CwtValue {
        if (element is CwtString) return changeContent(element, newContent, range)

        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue // not quoted here
        val newElement = CwtElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtString, newContent: String, range: TextRange? = null): CwtString {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue.quoteIfNeeded(QuotePatterns.Cwt)
        val newElement = CwtElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
