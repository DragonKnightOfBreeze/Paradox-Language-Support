package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.quote
import icu.windea.pls.core.replaceAndQuoteIfNeeded

object ParadoxScriptPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "@#=<>!?{}[\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it.isWhitespace() || it in FORCE_QUOTED_CHARS }
    }

    fun quoteIfNeeded(expression: String): String {
        return if (needQuote(expression)) expression.quote() else expression
    }

    fun changeContent(element: ParadoxScriptPropertyKey, range: TextRange, newContent: String): ParadoxScriptPropertyKey {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = ParadoxScriptElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptValue, range: TextRange, newContent: String): ParadoxScriptValue {
        if(element is ParadoxScriptString) return changeContent(element, range, newContent)

        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxScriptElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptString, range: TextRange, newContent: String): ParadoxScriptString {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = ParadoxScriptElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptParameter, range: TextRange, newContent: String): ParadoxScriptParameter {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxScriptElementFactory.createParameterFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptInlineMathParameter, range: TextRange, newContent: String): ParadoxScriptInlineMathParameter {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = ParadoxScriptElementFactory.createInlineMathParameterFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
