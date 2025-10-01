package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.csv.psi.ParadoxCsvColumn

class ParadoxCsvColumnTreeElement(
    element: ParadoxCsvColumn
) : ParadoxCsvTreeElement<ParadoxCsvColumn>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement?> {
        return emptyList()
    }
}
