package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock

class ParadoxScriptConditionalBlockTreeElement(
    element: ParadoxScriptConditionalBlock
) : ParadoxScriptTreeElement<ParadoxScriptConditionalBlock>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.children
        return children.mapNotNull { it.toTreeElement() }
    }
}
