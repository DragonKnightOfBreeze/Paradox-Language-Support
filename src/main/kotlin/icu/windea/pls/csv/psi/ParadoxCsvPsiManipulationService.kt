package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.quote

object ParadoxCsvPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#;\""

    fun needQuote(expression: String): Boolean {
        val s = expression
        if (s.isEmpty() || s == "\"") return true
        val lastIndex = s.lastIndex
        s.forEachIndexed f@{ i, c ->
            if ((i == 0 || i == lastIndex) && c == '\"') return@f
            // if (c.isWhitespace()) return true // whitespaces are allowed
            if (c in FORCE_QUOTED_CHARS) return true
        }
        return false
    }

    fun quoteIfNeeded(expression: String): String {
        if (expression.isLeftQuoted() && expression.isRightQuoted()) return expression
        if (!needQuote(expression)) return expression
        return expression.quote(lenient = true)
    }

    fun changeContent(element: ParadoxCsvColumn, newContent: String, range: TextRange? = null): ParadoxCsvColumn {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = quoteIfNeeded(newValue)
        val newElement = ParadoxCsvElementFactory.createColumnFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
