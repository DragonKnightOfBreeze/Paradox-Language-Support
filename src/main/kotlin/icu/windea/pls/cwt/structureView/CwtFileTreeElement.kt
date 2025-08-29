package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.forEachChild
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

class CwtFileTreeElement(
    element: CwtFile
) : CwtTreeElement<CwtFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val rootBlock = element.block ?: return emptyList()
        val result = mutableListOf<StructureViewTreeElement>()
        rootBlock.forEachChild {
            when (it) {
                is CwtProperty -> result.add(CwtPropertyTreeElement(it))
                is CwtValue -> result.add(CwtValueTreeElement(it))
            }
        }
        return result
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name
    }
}
