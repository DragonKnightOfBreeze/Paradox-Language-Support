package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.cast
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.script.text.ParadoxScript

/**
 * @see ElementDescriptionProvider
 */
object ParadoxScriptElementManipulationService {
    fun changeContent(element: ParadoxScriptExpressionElement, newContent: String, range: TextRange? = null): ParadoxScriptExpressionElement {
        if (element is ParadoxScriptPropertyKey) return changeContent(element, newContent, range)
        if (element is ParadoxScriptValue) return changeContent(element, newContent, range)
        throw UnsupportedOperationException()
    }

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

    fun changeContent(element: ParadoxScriptArgument, newContent: String, range: TextRange? = null): ParadoxScriptArgument {
        if (element is ParadoxScriptNormalParameterArgument) return changeContent(element, newContent, range)
        if (element is ParadoxScriptInlineMathParameterArgument) return changeContent(element, newContent, range)
        throw UnsupportedOperationException()
    }

    fun changeContent(element: ParadoxScriptNormalParameterArgument, newContent: String, range: TextRange? = null): ParadoxScriptNormalParameterArgument {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue
        val newElement = ParadoxScriptElementFactory.createNormalParameter(element.project, "p", newText).argumentElement ?: throw IncorrectOperationException()
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxScriptInlineMathParameterArgument, newContent: String, range: TextRange? = null): ParadoxScriptInlineMathParameterArgument {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue
        val newElement = ParadoxScriptElementFactory.createInlineMathParameter(element.project, "p", newText).argumentElement ?: throw IncorrectOperationException()
        return element.replace(newElement).cast()
    }
}
