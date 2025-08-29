package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.forEachChild
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

class CwtPropertyTreeElement(
    element: CwtProperty
) : CwtTreeElement<CwtProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val value = element.propertyValue ?: return emptyList()
        if (value !is CwtBlock) return emptyList()
        //允许混合value和property
        val result: MutableList<StructureViewTreeElement> = mutableListOf()
        value.forEachChild {
            when {
                it is CwtValue -> result.add(CwtValueTreeElement(it))
                it is CwtProperty -> result.add(CwtPropertyTreeElement(it))
            }
        }
        return result
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name
    }
}

