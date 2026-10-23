package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.cwt.psi.CwtFile

class CwtFileTreeElement(
    element: CwtFile,
) : CwtTreeElement<CwtFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.block?.children?.orNull() ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
