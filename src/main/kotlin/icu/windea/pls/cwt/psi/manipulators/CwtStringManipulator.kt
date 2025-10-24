package icu.windea.pls.cwt.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNecessary
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtString

class CwtStringManipulator : AbstractElementManipulator<CwtString>() {
    override fun handleContentChange(element: CwtString, range: TextRange, newContent: String): CwtString {
        val text = element.text
        val newText = range.replaceAndQuoteIfNecessary(text, newContent)
        val newElement = CwtElementFactory.createString(element.project, newText)
        return element.replace(newElement).cast()
    }
}
