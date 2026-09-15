package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.propertyValue

class CwtPropertyTreeElement(
    element: CwtProperty
) : CwtTreeElement<CwtProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.propertyValue<CwtBlock>()?.children ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
