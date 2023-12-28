package icu.windea.pls.cwt.psi.manipulators

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

class CwtPropertyKeyExpressionManipulator: AbstractElementManipulator<CwtPropertyKey>() {
    override fun handleContentChange(element: CwtPropertyKey, range: TextRange, newContent: String): CwtPropertyKey {
        val text = element.text
        val newText = range.replace(text, newContent)
            .let { it.unquote().quoteIfNecessary(or = it.isQuoted()) }
        val newElement = CwtElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class CwtValueManipulator: AbstractElementManipulator<CwtValue>() {
    override fun handleContentChange(element: CwtValue, range: TextRange, newContent: String): CwtValue {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = CwtElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class CwtStringManipulator: AbstractElementManipulator<CwtString>() {
    override fun handleContentChange(element: CwtString, range: TextRange, newContent: String): CwtString {
        val text = element.text
        val newText = range.replace(text, newContent)
            .let { it.unquote().quoteIfNecessary(or = it.isQuoted()) }
        val newElement = CwtElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}