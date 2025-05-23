package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPropertyTreeElement(
    element: ParadoxLocalisationProperty
) : ParadoxLocalisationTreeElement<ParadoxLocalisationProperty>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        return emptyList()
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name
    }
}
