package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

class ParadoxLocalisationPropertyListTreeElement(
    element: ParadoxLocalisationPropertyList
) : ParadoxLocalisationTreeElement<ParadoxLocalisationPropertyList>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val properties = element.propertyList
        return properties.mapNotNull { it.toTreeElement() }
    }
}
