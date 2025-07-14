package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.settings.*

class ParadoxCsvColumnTreeElement(
    element: ParadoxCsvColumn
) : ParadoxCsvTreeElement<ParadoxCsvColumn>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement?> {
        return emptyList()
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        val headerColumn = element.getHeaderColumn() ?: return null
        return headerColumn.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit)
    }
}
