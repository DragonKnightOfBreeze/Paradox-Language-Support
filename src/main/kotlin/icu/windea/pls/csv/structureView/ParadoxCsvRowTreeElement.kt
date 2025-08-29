package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.model.constants.PlsStringConstants

class ParadoxCsvRowTreeElement(
    element: ParadoxCsvRow
) : ParadoxCsvTreeElement<ParadoxCsvRow>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement?> {
        val element = element ?: return emptyList()
        val columns = element.columnList
        if (columns.isEmpty()) return emptyList()
        return columns.map { ParadoxCsvColumnTreeElement(it) }
    }

    override fun getPresentableText(): String? {
        //val element = element ?: return null
        return PlsStringConstants.rowMarker
    }
}
