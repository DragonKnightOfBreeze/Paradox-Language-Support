package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvColumnTreeElement(
    element: ParadoxCsvColumn
) : ParadoxCsvTreeElement<ParadoxCsvColumn>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement?> {
        return emptyList()
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        val limit = PlsFacade.getInternalSettings().presentableTextLengthLimit
        return element.name.truncateAndKeepQuotes(limit)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        val headerColumn = element.getHeaderColumn() ?: return null
        val limit = PlsFacade.getInternalSettings().presentableTextLengthLimit
        return headerColumn.name.truncateAndKeepQuotes(limit)
    }
}
