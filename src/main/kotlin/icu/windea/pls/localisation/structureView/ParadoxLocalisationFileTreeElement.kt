package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

class ParadoxLocalisationFileTreeElement(
    element: ParadoxLocalisationFile,
) : ParadoxLocalisationTreeElement<ParadoxLocalisationFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val children = element.children.orNull() ?: return emptyList()
        return children.mapNotNull { it.toTreeElement() }
    }
}
