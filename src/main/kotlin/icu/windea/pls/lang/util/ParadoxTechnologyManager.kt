package icu.windea.pls.lang.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxTechnologyManager {
    fun getTechnologies(selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): Set<ParadoxScriptDefinitionElement> {
        return ParadoxDefinitionSearch.search("technology", selector).findAll()
    }

    fun getName(element: ParadoxScriptDefinitionElement): String {
        return element.name // = element.definitionInfo.name
    }

    fun getLocalizedName(definition: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    fun getIconFile(definition: ParadoxScriptDefinitionElement): PsiFile? {
        return ParadoxDefinitionManager.getPrimaryImage(definition)
    }

    @WithGameType(ParadoxGameType.Stellaris)
    object Stellaris {
        fun getTechnologyTiers(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
            val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
            return ParadoxDefinitionSearch.search("technology_tier", selector).findAll()
        }

        fun getResearchAreas(): Set<String> {
            return getConfigGroup(ParadoxGameType.Stellaris).enums.get("research_area")?.values.orEmpty()
        }

        fun getTechnologyCategories(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
            val selector = definitionSelector(project, context).withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
            return ParadoxDefinitionSearch.search("technology_category", selector).findAll()
        }
    }
}
