package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.config.CwtSubtypeGroup
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.ep.util.data.StellarisTechnologyData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.resolve.ParadoxTechnologyService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.withGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxTechnologyManager {
    object Keys : KeyRegistry() {
        val cachedPrerequisites by registerKey<CachedValue<Set<String>>>(Keys)
        val technologyAllAttributes by registerKey<Set<String>>(Keys)
        val technologyAttributes by registerKey<Set<String>>(Keys)
    }

    @Suppress("unused")
    fun getTechnologies(selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
        return ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.technology, selector).findAll()
    }

    fun getName(element: ParadoxDefinitionElement): String {
        return element.name // = element.definitionInfo.name
    }

    @Suppress("unused")
    fun getPresentableNameElement(definition: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    @Suppress("unused")
    fun getIconFile(definition: ParadoxDefinitionElement): PsiFile? {
        return ParadoxDefinitionManager.getPrimaryImage(definition)
    }

    @ForGameType(ParadoxGameType.Stellaris)
    object Stellaris {
        private val gameType = ParadoxGameType.Stellaris

        fun getAllTiers(project: Project, context: Any?): List<ParadoxScriptProperty> {
            val selector = ParadoxDefinitionSearch.selector(project, context).withGameType(gameType).contextSensitive().distinct()
            return ParadoxDefinitionSearch.searchProperty(null, "technology_tier", selector).findAll()
        }

        fun getAllResearchAreas(): Set<String> {
            return ChronicleFacade.getConfigGroup(gameType).enums.get("research_area")?.values.orEmpty()
        }

        fun getAllResearchAreaConfigs(project: Project): Collection<CwtValueConfig> {
            return ChronicleFacade.getConfigGroup(project, gameType).enums.get("research_area")?.valueConfigMap?.values.orEmpty()
        }

        fun getAllCategories(project: Project, context: Any?): List<ParadoxScriptProperty> {
            val selector = ParadoxDefinitionSearch.selector(project, context).withGameType(gameType).contextSensitive().distinct()
            return ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.technologyCategory, selector).findAll()
        }

        fun getAllAttributes(gameType: ParadoxGameType): Set<String> {
            val eventConfig = ChronicleFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.technology] ?: return emptySet()
            return eventConfig.config.getOrPutUserData(Keys.technologyAllAttributes) {
                val result = eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.TechnologyAttribute }.map { it.name }.toSet()
                result.optimized()
            }
        }

        @Suppress("unused")
        fun getAllAttributeConfigs(project: Project): Collection<CwtSubtypeConfig> {
            val eventConfig = ChronicleFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.technology] ?: return emptySet()
            val result = eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.TechnologyAttribute }
            return result
        }

        fun getTier(element: ParadoxDefinitionElement): String? {
            val result = element.getDefinitionData<StellarisTechnologyData>()?.tier
            return result
        }

        fun getArea(element: ParadoxDefinitionElement): String? {
            val result = element.getDefinitionData<StellarisTechnologyData>()?.area
            return result
        }

        fun getCategories(element: ParadoxDefinitionElement): Set<String> {
            val result = element.getDefinitionData<StellarisTechnologyData>()?.category
            return result.orEmpty()
        }

        @Suppress("unused")
        fun getAttributes(element: ParadoxDefinitionElement): Set<String> {
            val result = element.definitionInfo?.let { getAttributes(it) }
            return result.orEmpty()
        }

        fun getAttributes(definitionInfo: ParadoxDefinitionInfo): Set<String> {
            return definitionInfo.getOrPutUserData(Keys.technologyAttributes) {
                val result = definitionInfo.subtypeConfigs.filter { it in CwtSubtypeGroup.TechnologyAttribute }.map { it.name }.toSet()
                result.optimized()
            }
        }

        /**
         * 得到指定科技的作为其前提条件的所有科技的名字。
         */
        fun getPrerequisites(definition: ParadoxDefinitionElement): Set<String> {
            // from cache
            return CachedValuesManager.getCachedValue(definition, Keys.cachedPrerequisites) {
                val value = ParadoxTechnologyService.Stellaris.resolvePrerequisites(definition).optimized()
                CachedValueProvider.Result(value, definition)
            }
        }

        /**
         * 得到作为前提条件的科技列表。
         */
        fun getPreTechnologies(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
            return ParadoxTechnologyService.Stellaris.resolvePreTechnologies(definition, selector)
        }

        /**
         * 得到后续的科技列表。
         */
        fun getPostTechnologies(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
            return ParadoxTechnologyService.Stellaris.resolvePostTechnologies(definition, selector)
        }
    }
}
