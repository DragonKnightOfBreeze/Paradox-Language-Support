package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty

class CwtPropertyTreeElement(
    element: CwtProperty
) : CwtTreeElement<CwtProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.propertyValue?.castOrNull<CwtBlock>()?.children ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
