package icu.windea.pls.lang

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
@WithGameType(ParadoxGameType.Stellaris)
object ParadoxTechnologyHandler {
    fun supports(project: Project, context: Any?): Boolean {
        val gameType = selectGameType(context)
        return gameType == ParadoxGameType.Stellaris
    }
    
    @JvmStatic
    fun getTechnologies(project: Project, context: Any?): Set<ParadoxScriptProperty> {
        val selector = definitionSelector(project, context).contextSensitive().distinctByName()
        val technologies = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("technology", selector).processQuery {
            if(it is ParadoxScriptProperty) technologies.add(it)
            true
        }
        return technologies
    }
    
    @JvmStatic
    fun getStartTechnologies(project: Project, context: Any?): Set<ParadoxScriptProperty> {
        val selector = definitionSelector(project, context).contextSensitive().distinctByName()
        val technologies = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("technology", selector).processQuery {
            if(it is ParadoxScriptProperty && isStartTechnology(it)) technologies.add(it)
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
    fun getPrerequisites(element: ParadoxScriptProperty): Set<String> {
        return element.findProperty("prerequisites")?.blockValues<ParadoxScriptString>()
            ?.mapTo(mutableSetOf()) { it.stringValue }
            .orEmpty()
    }
}