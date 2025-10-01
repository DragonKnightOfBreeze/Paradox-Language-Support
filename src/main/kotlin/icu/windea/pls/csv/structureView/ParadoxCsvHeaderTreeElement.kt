package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.csv.psi.ParadoxCsvHeader

class ParadoxCsvHeaderTreeElement(
    element: ParadoxCsvHeader
) : ParadoxCsvTreeElement<ParadoxCsvHeader>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val columns = element.columnList
        return columns.mapNotNull { it.toTreeElement() }
    }
}
