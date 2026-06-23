package icu.windea.pls.script.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNeeded
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptString

class ParadoxScriptStringManipulator : AbstractElementManipulator<ParadoxScriptString>() {
    override fun handleContentChange(element: ParadoxScriptString, range: TextRange, newContent: String): ParadoxScriptString {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = ParadoxScriptElementFactory.createString(element.project, newText)
        return element.replace(newElement).cast()
    }
}
