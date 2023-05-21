package icu.windea.pls.lang

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object StellarisTechnologyHandler : ParadoxTechnologyHandler() {
    fun getTechnologyTiers(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
        val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
        return ParadoxDefinitionSearch.search("technology_tier", selector).findAll()
    }
    
    fun getResearchAreas(): Set<String> {
        return getCwtConfig().stellaris.enums.get("research_area")?.values.orEmpty()
    }
    
    fun getTechnologyCategories(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
        val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
        return ParadoxDefinitionSearch.search("technology_category", selector).findAll()
    }
}