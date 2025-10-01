package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.script.psi.ParadoxScriptValue

class ParadoxScriptValueTreeElement(
    element: ParadoxScriptValue
) : ParadoxScriptTreeElement<ParadoxScriptValue>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.children
        return children.mapNotNull { it.toTreeElement() }
    }
}
