package icu.windea.pls.cwt.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNeeded
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtPropertyKey

class CwtPropertyKeyManipulator : AbstractElementManipulator<CwtPropertyKey>() {
    override fun handleContentChange(element: CwtPropertyKey, range: TextRange, newContent: String): CwtPropertyKey {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = CwtElementFactory.createPropertyKey(element.project, newText)
        return element.replace(newElement).cast()
    }
}
