package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.cwt.psi.CwtValue

class CwtValueTreeElement(
    element: CwtValue,
) : CwtTreeElement<CwtValue>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.children.orNull() ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
