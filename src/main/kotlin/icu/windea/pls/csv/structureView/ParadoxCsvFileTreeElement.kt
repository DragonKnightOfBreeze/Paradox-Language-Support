package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.csv.psi.ParadoxCsvFile

class ParadoxCsvFileTreeElement(
    element: ParadoxCsvFile
) : ParadoxCsvTreeElement<ParadoxCsvFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.children
        return children.mapNotNull { it.toTreeElement() }
    }
}
