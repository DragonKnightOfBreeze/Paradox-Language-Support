package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.csv.psi.ParadoxCsvRow

class ParadoxCsvRowTreeElement(
    element: ParadoxCsvRow,
) : ParadoxCsvTreeElement<ParadoxCsvRow>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val columns = element.columnList.orNull() ?: return emptyList()
        return columns.mapNotNull { it.toTreeElement() }
    }
}
