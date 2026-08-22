package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.quote

object CwtPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#={}\""

    fun needQuote(expression: String): Boolean {
        val s = expression
        if (s.isEmpty() || s == "\"") return true
        val lastIndex = s.lastIndex
        s.forEachIndexed f@{ i, c ->
            if ((i == 0 || i == lastIndex) && c == '\"') return@f
            if (c.isWhitespace()) return true // whitespaces are not allowed
            if (c in FORCE_QUOTED_CHARS) return true
        }
        return false
    }

    fun quoteIfNeeded(expression: String): String {
        if (expression.isLeftQuoted() && expression.isRightQuoted()) return expression
        if (!needQuote(expression)) return expression
        return expression.quote(lenient = true)
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
