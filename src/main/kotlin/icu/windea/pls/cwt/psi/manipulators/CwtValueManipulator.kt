package icu.windea.pls.cwt.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtValue

class CwtValueManipulator : AbstractElementManipulator<CwtValue>() {
    override fun handleContentChange(element: CwtValue, range: TextRange, newContent: String): CwtValue {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = CwtElementFactory.createValue(element.project, newText)
        return element.replace(newElement).cast()
    }
}
