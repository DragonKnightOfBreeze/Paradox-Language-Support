package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

class ParadoxLocalisationFileTreeElement(
    element: ParadoxLocalisationFile
) : ParadoxLocalisationTreeElement<ParadoxLocalisationFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val propertyLists = element.propertyLists
        return propertyLists.mapNotNull { it.toTreeElement() }
    }
}
