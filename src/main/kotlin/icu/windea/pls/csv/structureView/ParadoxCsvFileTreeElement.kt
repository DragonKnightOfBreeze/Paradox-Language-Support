package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvFileTreeElement(
    element: ParadoxCsvFile
) : ParadoxCsvTreeElement<ParadoxCsvFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val rows = element.rows
        if (rows.isEmpty()) return emptyList()
        return rows.map { ParadoxCsvRowTreeElement(it) }
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name
    }
}
