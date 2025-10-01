package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertyTreeElement(
    element: ParadoxScriptProperty
) : ParadoxScriptTreeElement<ParadoxScriptProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.propertyValue?.castOrNull<ParadoxScriptBlock>()?.children ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
