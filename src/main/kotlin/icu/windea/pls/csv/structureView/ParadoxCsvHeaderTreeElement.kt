package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.model.constants.*

class ParadoxCsvHeaderTreeElement(
    element: ParadoxCsvHeader
) : ParadoxCsvTreeElement<ParadoxCsvHeader>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement?> {
        val element = element ?: return emptyList()
        val columns = element.columnList
        if (columns.isEmpty()) return emptyList()
        return columns.map { ParadoxCsvColumnTreeElement(it) }
    }

    override fun getPresentableText(): String? {
        //val element = element ?: return null
        return PlsStringConstants.headerMarker
    }
}
