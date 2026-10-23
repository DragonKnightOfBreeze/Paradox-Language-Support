package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.propertyValue

class ParadoxScriptPropertyTreeElement(
    element: ParadoxScriptProperty,
) : ParadoxScriptTreeElement<ParadoxScriptProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.propertyValue<ParadoxScriptBlock>()?.children?.orNull() ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
