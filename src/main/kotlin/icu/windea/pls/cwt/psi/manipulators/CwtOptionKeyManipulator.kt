package icu.windea.pls.cwt.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNecessary
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtOptionKey

class CwtOptionKeyManipulator : AbstractElementManipulator<CwtOptionKey>() {
    override fun handleContentChange(element: CwtOptionKey, range: TextRange, newContent: String): CwtOptionKey {
        val text = element.text
        val newText = range.replaceAndQuoteIfNecessary(text, newContent)
        val newElement = CwtElementFactory.createOptionKey(element.project, newText)
        return element.replace(newElement).cast()
    }
}
