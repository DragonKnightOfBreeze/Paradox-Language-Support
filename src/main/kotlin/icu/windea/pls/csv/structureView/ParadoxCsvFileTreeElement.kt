package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import icu.windea.pls.core.forEachChild
import icu.windea.pls.csv.psi.*

class ParadoxCsvFileTreeElement(
    element: ParadoxCsvFile
) : ParadoxCsvTreeElement<ParadoxCsvFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val result = mutableListOf<StructureViewTreeElement>()
        element.forEachChild { it ->
            when (it) {
                is ParadoxCsvHeader -> result.add(ParadoxCsvHeaderTreeElement(it))
                is ParadoxCsvRow -> result.add(ParadoxCsvRowTreeElement(it))
            }
        }
        return result
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name
    }
}
