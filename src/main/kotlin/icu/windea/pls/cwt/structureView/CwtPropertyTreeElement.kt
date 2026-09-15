package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.cwt.psi.CwtProperty

class CwtPropertyTreeElement(
    element: CwtProperty
) : CwtTreeElement<CwtProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val members = element.members ?: return emptyList()
        return members.mapNotNull { it.toTreeElement() }
    }
}
