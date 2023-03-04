package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.model.*
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
    fun isStartTechnology(element: ParadoxScriptProperty): Boolean {
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.type == "technology" && definitionInfo.subtypes.contains("start_tech")
    }
    
    @JvmStatic
    fun getName(element: ParadoxScriptProperty): String {
        return element.name // = element.definitionInfo.name
    }
    
    @JvmStatic
    fun getIconFile(definition: ParadoxScriptProperty): PsiFile? {
        val definitionInfo = definition.definitionInfo ?: return null
        return definitionInfo.primaryImages.firstNotNullOfOrNull block@{
            val resolved = it.locationExpression.resolve(definition, definitionInfo, definitionInfo.project)
            if(resolved == null) return@block null
            definition.putUserData(PlsKeys.iconFrame, resolved.frame)
            resolved.file
        }
    }
    
    @JvmStatic
    fun getPrerequisites(element: ParadoxScriptProperty): Set<String> {
        return element.findProperty("prerequisites", inline = true)?.valueList
            ?.mapNotNullTo(mutableSetOf()) { it.stringValue() }
            .orEmpty()
    }
}