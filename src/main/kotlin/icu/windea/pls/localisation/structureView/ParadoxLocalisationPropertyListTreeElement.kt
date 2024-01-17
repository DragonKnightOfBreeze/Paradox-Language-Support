package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPropertyListTreeElement(
    element: ParadoxLocalisationPropertyList
) : PsiTreeElementBase<ParadoxLocalisationPropertyList>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val result = mutableListOf<StructureViewTreeElement>()
        element.forEachChild {
            if(it is ParadoxLocalisationProperty) result.add(ParadoxLocalisationPropertyTreeElement(it))
        }
        return result
    }
    
    override fun getPresentableText(): String? {
        val locale = element?.locale ?: return null
        return locale.name
    }
    
    override fun getLocationString(): String? {
        val localeConfig = element?.let { selectLocale(it) } ?: return null
        return localeConfig.description
    }
}