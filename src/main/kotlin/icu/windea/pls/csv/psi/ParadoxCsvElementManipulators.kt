package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNecessary
import icu.windea.pls.lang.util.ParadoxCsvManager

class ParadoxCsvColumnManipulator : AbstractElementManipulator<ParadoxCsvColumn>() {
    override fun handleContentChange(element: ParadoxCsvColumn, range: TextRange, newContent: String): ParadoxCsvColumn {
        val text = element.text
        val extraChars = ParadoxCsvManager.getSeparator().toString()
        val newText = range.replaceAndQuoteIfNecessary(text, newContent, extraChars = extraChars, blank = false)
        val newElement = ParadoxCsvElementFactory.createColumn(element.project, newText)
        return element.replace(newElement).cast()
    }
}
