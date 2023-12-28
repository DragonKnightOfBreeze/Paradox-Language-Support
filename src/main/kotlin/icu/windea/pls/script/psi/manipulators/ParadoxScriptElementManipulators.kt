package icu.windea.pls.script.psi.manipulators

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*

class ParadoxScriptPropertyKeyExpressionManipulator: AbstractElementManipulator<ParadoxScriptPropertyKey>() {
    override fun handleContentChange(element: ParadoxScriptPropertyKey, range: TextRange, newContent: String): ParadoxScriptPropertyKey {
        val text = element.text
        val newText = range.replace(text, newContent)
            .let { it.quoteIfNecessary(or = it.isQuoted()) }
        val newElement = ParadoxScriptElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptValueManipulator: AbstractElementManipulator<ParadoxScriptValue>() {
    override fun handleContentChange(element: ParadoxScriptValue, range: TextRange, newContent: String): ParadoxScriptValue {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxScriptElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptStringManipulator: AbstractElementManipulator<ParadoxScriptString>() {
    override fun handleContentChange(element: ParadoxScriptString, range: TextRange, newContent: String): ParadoxScriptString {
        val text = element.text
        val newText = range.replace(text, newContent)
            .let { it.quoteIfNecessary(or = it.isQuoted()) }
        val newElement = ParadoxScriptElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptParameterManipulator: AbstractElementManipulator<ParadoxScriptParameter>() {
    override fun handleContentChange(element: ParadoxScriptParameter, range: TextRange, newContent: String): ParadoxScriptParameter {
        val text = element.text
        val newText = range.replace(text, newContent)
            .let { it.quoteIfNecessary(or = it.isQuoted()) }
            .unquote() //unquote at last to escape double quotes
        val newElement = ParadoxScriptElementFactory.createParameterFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptInlineMathParameterManipulator: AbstractElementManipulator<ParadoxScriptInlineMathParameter>() {
    override fun handleContentChange(element: ParadoxScriptInlineMathParameter, range: TextRange, newContent: String): ParadoxScriptInlineMathParameter {
        val text = element.text
        val newText = range.replace(text, newContent)
            .let { it.quoteIfNecessary(or = it.isQuoted()) }
            .unquote() //unquote at last to escape double quotes
        val newElement = ParadoxScriptElementFactory.createInlineMathParameterFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}