package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.script.text.ParadoxScript

object ParadoxScriptPsiManipulationService {
    fun changeContent(element: ParadoxScriptPropertyKey, newContent: String, range: TextRange? = null): ParadoxScriptPropertyKey {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue.quoteIfNeeded(QuotePatterns.ParadoxScript)
        val newElement = ParadoxScriptElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptValue, newContent: String, range: TextRange? = null): ParadoxScriptValue {
        if (element is ParadoxScriptString) return changeContent(element, newContent, range)

        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue // not quoted here
        val newElement = ParadoxScriptElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptString, newContent: String, range: TextRange? = null): ParadoxScriptString {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue.quoteIfNeeded(QuotePatterns.ParadoxScript)
        val newElement = ParadoxScriptElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
