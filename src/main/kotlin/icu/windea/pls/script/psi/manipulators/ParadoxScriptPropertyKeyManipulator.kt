package icu.windea.pls.script.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNeeded
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

class ParadoxScriptPropertyKeyManipulator : AbstractElementManipulator<ParadoxScriptPropertyKey>() {
    override fun handleContentChange(element: ParadoxScriptPropertyKey, range: TextRange, newContent: String): ParadoxScriptPropertyKey {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = ParadoxScriptElementFactory.createPropertyKey(element.project, newText)
        return element.replace(newElement).cast()
    }
}

