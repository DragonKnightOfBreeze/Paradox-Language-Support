package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationFileTreeElement(
    element: ParadoxLocalisationFile
) : PsiTreeElementBase<ParadoxLocalisationFile>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val propertyLists = element.propertyLists
        if (propertyLists.isEmpty()) return emptyList()
        return propertyLists.map { ParadoxLocalisationPropertyListTreeElement(it) }
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return element.name
    }
}

