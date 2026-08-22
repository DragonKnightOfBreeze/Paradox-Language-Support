package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote

object ParadoxScriptPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "@#=<>!?{}[\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it.isWhitespace() || it in FORCE_QUOTED_CHARS }
    }

    fun quoteIfNeeded(expression: String): String {
        if (expression.isLeftQuoted() && expression.isRightQuoted()) return expression
        if (!needQuote(expression)) return expression
        return expression.unquote().quote() // unquote first
    }

    fun changeContent(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionElement {

    }

    fun changeContent(element: ParadoxScriptPropertyKey, newContent: String, range: TextRange? = null): ParadoxScriptPropertyKey {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = quoteIfNeeded(newValue)
        val newElement = ParadoxScriptElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptValue, newContent: String, range: TextRange? = null): ParadoxScriptValue {
        if(element is ParadoxScriptString) return changeContent(element, newContent, range)

        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue // not quoted here
        val newElement = ParadoxScriptElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptString, newContent: String, range: TextRange? = null): ParadoxScriptString {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = quoteIfNeeded(newValue)
        val newElement = ParadoxScriptElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
