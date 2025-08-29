package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.getHeaderColumn

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
