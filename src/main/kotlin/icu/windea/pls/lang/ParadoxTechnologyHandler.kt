package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

open class ParadoxTechnologyHandler {
    companion object INSTANCE: ParadoxTechnologyHandler()
    
    fun getTechnologies(selector: ParadoxDefinitionSelector): Set<ParadoxScriptProperty> {
        val result = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("technology", selector).processQuery {
            if(it is ParadoxScriptProperty) result.add(it)
            true
        }
        return result
    }
    
    fun getName(element: ParadoxScriptProperty): String {
        return element.name // = element.definitionInfo.name
    }
    
    fun getLocalizedName(definition: ParadoxScriptProperty): ParadoxLocalisationProperty? {
        return definition.definitionInfo?.resolvePrimaryLocalisation()
    }
    
    fun getIconFile(definition: ParadoxScriptProperty): PsiFile? {
        return definition.definitionInfo?.resolvePrimaryImage()
    }
    
    /**
     * 得到指定科技的所有前置科技。
     */
    fun getPrerequisites(definition: ParadoxScriptProperty): Set<String> {
        val data = definition.getData<StellarisTechnologyDataProvider.Data>() ?: return emptySet()
        val prerequisites = data.prerequisites
        return prerequisites
    }
}