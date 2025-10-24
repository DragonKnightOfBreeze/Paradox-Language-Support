package icu.windea.pls.script.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptValue

class ParadoxScriptValueManipulator : AbstractElementManipulator<ParadoxScriptValue>() {
    override fun handleContentChange(element: ParadoxScriptValue, range: TextRange, newContent: String): ParadoxScriptValue {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxScriptElementFactory.createValue(element.project, newText)
        return element.replace(newElement).cast()
    }
}
