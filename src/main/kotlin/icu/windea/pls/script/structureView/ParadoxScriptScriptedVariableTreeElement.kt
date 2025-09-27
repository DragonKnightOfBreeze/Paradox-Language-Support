package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptScriptedVariableTreeElement(
    element: ParadoxScriptScriptedVariable
) : ParadoxScriptTreeElement<ParadoxScriptScriptedVariable>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        return emptyList()
    }
}
