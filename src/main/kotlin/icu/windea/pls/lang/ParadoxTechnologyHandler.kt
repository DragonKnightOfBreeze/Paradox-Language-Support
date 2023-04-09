package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

open class ParadoxTechnologyHandler {
    companion object INSTANCE: ParadoxTechnologyHandler()
    
    fun getTechnologies(selector: ParadoxDefinitionSelector): Set<ParadoxScriptDefinitionElement> {
        return ParadoxDefinitionSearch.search("technology", selector).findAll()
    }
    
    fun getName(element: ParadoxScriptDefinitionElement): String {
        return element.name // = element.definitionInfo.name
    }
    
    fun getLocalizedName(definition: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return definition.definitionInfo?.resolvePrimaryLocalisation()
    }
    
    fun getIconFile(definition: ParadoxScriptDefinitionElement): PsiFile? {
        return definition.definitionInfo?.resolvePrimaryImage()
    }
}