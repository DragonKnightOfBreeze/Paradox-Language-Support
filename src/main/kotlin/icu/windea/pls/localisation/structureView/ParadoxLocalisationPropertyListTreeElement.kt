package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.core.forEachChild
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

class ParadoxLocalisationPropertyListTreeElement(
    element: ParadoxLocalisationPropertyList
) : ParadoxLocalisationTreeElement<ParadoxLocalisationPropertyList>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val result = mutableListOf<StructureViewTreeElement>()
        element.forEachChild {
            if (it is ParadoxLocalisationProperty) result.add(ParadoxLocalisationPropertyTreeElement(it))
        }
        return result
    }

    override fun getPresentableText(): String? {
        val locale = element?.locale ?: return null
        return locale.name
    }

    override fun getLocationString(): String? {
        val localeConfig = element?.let { selectLocale(it) } ?: return null
        return localeConfig.text
    }
}
