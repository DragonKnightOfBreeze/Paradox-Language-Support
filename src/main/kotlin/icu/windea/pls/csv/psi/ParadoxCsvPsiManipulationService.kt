package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementDescriptionProvider
import icu.windea.pls.core.cast
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.csv.text.ParadoxCsv

/**
 * @see ElementDescriptionProvider
 */
object ParadoxCsvPsiManipulationService {
    fun changeContent(element: ParadoxCsvColumn, newContent: String, range: TextRange? = null): ParadoxCsvColumn {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue.quoteIfNeeded(QuotePatterns.ParadoxCsv)
        val newElement = ParadoxCsvElementFactory.createColumnFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
