package icu.windea.pls.csv.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.replaceAndQuoteIfNeeded
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementFactory
import icu.windea.pls.csv.psi.ParadoxCsvPsiService

class ParadoxCsvColumnManipulator : AbstractElementManipulator<ParadoxCsvColumn>() {
    override fun handleContentChange(element: ParadoxCsvColumn, range: TextRange, newContent: String): ParadoxCsvColumn {
        val text = element.text
        val extraChars = ParadoxCsvPsiService.getSeparator().toString()
        val newText = range.replaceAndQuoteIfNeeded(text, newContent, containAnyChar = extraChars, containBlank = false)
        val newElement = ParadoxCsvElementFactory.createColumnFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
