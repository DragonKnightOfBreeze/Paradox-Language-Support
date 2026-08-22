package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote

object CwtPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#={}\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it.isWhitespace() || it in FORCE_QUOTED_CHARS }
    }

    fun quoteIfNeeded(expression: String): String {
        if (expression.isLeftQuoted() && expression.isRightQuoted()) return expression
        if (!needQuote(expression)) return expression
        return expression.unquote().quote() // unquote first
    }

    fun changeContent(element: CwtOptionKey, newContent: String, range: TextRange? = null): CwtOptionKey {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = quoteIfNeeded(newValue)
        val newElement = CwtElementFactory.createOptionKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtPropertyKey, newContent: String, range: TextRange? = null): CwtPropertyKey {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = quoteIfNeeded(newValue)
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
        val newText = quoteIfNeeded(newValue)
        val newElement = CwtElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
