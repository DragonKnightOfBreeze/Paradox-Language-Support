package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object StellarisTechnologyHandler {
    @JvmStatic
    fun getTechnologies(project: Project, context: Any?): Set<ParadoxScriptProperty> {
        val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
        val technologies = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("technology", selector).processQuery {
            if(it is ParadoxScriptProperty) technologies.add(it)
            true
        }
        return technologies
    }
    
    @JvmStatic
    fun getTechnologyTiers(project: Project, context: Any?): Set<ParadoxScriptProperty> {
        val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
        val technologies = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("technology_tier", selector).processQuery {
            if(it is ParadoxScriptProperty) technologies.add(it)
            true
        }
        return technologies
    }
    
    @JvmStatic
    fun getResearchAreas(): Set<String> {
        return getCwtConfig().stellaris.enums.get("research_areas")?.values.orEmpty()
    }
    
    @JvmStatic
    fun getTechnologyCategories(project: Project, context: Any?): Set<ParadoxScriptProperty> {
        val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
        val technologies = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("technology_category", selector).processQuery {
            if(it is ParadoxScriptProperty) technologies.add(it)
            true
        }
        return technologies
    }
    
    @JvmStatic
    fun getName(element: ParadoxScriptProperty): String {
        return element.name // = element.definitionInfo.name
    }
    
    @JvmStatic
    fun getLocalizedName(definition: ParadoxScriptProperty): ParadoxLocalisationProperty? {
        return definition.definitionInfo?.resolvePrimaryLocalisation()
    }
    
    @JvmStatic
    fun getIconFile(definition: ParadoxScriptProperty): PsiFile? {
        return definition.definitionInfo?.resolvePrimaryImage()
    }
    
    /**
     * 得到指定科技的所有前置科技。
     */
    @JvmStatic
    fun getPrerequisites(definition: ParadoxScriptProperty): Set<String> {
        val data = definition.getData<StellarisTechnologyDataProvider.Data>() ?: return emptySet()
        val prerequisites = data.prerequisites
        return prerequisites
    }
}