package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.cwt.psi.CwtFile

class CwtFileTreeElement(
    element: CwtFile
) : CwtTreeElement<CwtFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val members = element.members
        return members.mapNotNull { it.toTreeElement() }
    }
}
