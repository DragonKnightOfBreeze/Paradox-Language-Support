@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtSubtypeConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.processQuery
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.ep.data.StellarisTechnologyData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getData
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.lang.util.data.get
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findParentByPath

object ParadoxTechnologyManager {
    object Keys : KeyRegistry() {
        val cachedPrerequisites by createKey<CachedValue<Set<String>>>(Keys)
        val technologyAllAttributes by createKey<Set<String>>(Keys)
        val technologyAttributes by createKey<Set<String>>(Keys)
    }

    fun getTechnologies(selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): Set<ParadoxScriptDefinitionElement> {
        return ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.Technology, selector).findAll()
    }

    fun getName(element: ParadoxScriptDefinitionElement): String {
        return element.name // = element.definitionInfo.name
    }

    fun getLocalizedNameElement(definition: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    fun getIconFile(definition: ParadoxScriptDefinitionElement): PsiFile? {
        return ParadoxDefinitionManager.getPrimaryImage(definition)
    }

    @WithGameType(ParadoxGameType.Stellaris)
    object Stellaris {
        private val gameType = ParadoxGameType.Stellaris

        fun getAllTiers(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
            val selector = selector(project, context).definition().withGameType(gameType).contextSensitive().distinctByName()
            return ParadoxDefinitionSearch.search("technology_tier", selector).findAll()
        }

        fun getAllResearchAreas(): Set<String> {
            return PlsFacade.getConfigGroup(gameType).enums.get("research_area")?.values.orEmpty()
        }

        fun getAllResearchAreaConfigs(project: Project): Collection<CwtValueConfig> {
            return PlsFacade.getConfigGroup(project, gameType).enums.get("research_area")?.valueConfigMap?.values.orEmpty()
        }

        fun getAllCategories(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
            val selector = selector(project, context).definition().withGameType(gameType).contextSensitive().distinctByName()
            return ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.TechnologyCategory, selector).findAll()
        }

        fun getAllAttributes(gameType: ParadoxGameType): Set<String> {
            val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.Technology] ?: return emptySet()
            return eventConfig.config.getOrPutUserData(Keys.technologyAllAttributes) {
                eventConfig.subtypes.values.filter { it.inGroup("technology_attribute") }.map { it.name }.toSet()
            }
        }

        fun getAllAttributeConfigs(project: Project): Collection<CwtSubtypeConfig> {
            val eventConfig = PlsFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.Technology] ?: return emptySet()
            return eventConfig.subtypes.values.filter { it.inGroup("technology_attribute") }
        }

        fun getTier(element: ParadoxScriptDefinitionElement): String? {
            return element.getData<StellarisTechnologyData>()?.tier
        }

        fun getArea(element: ParadoxScriptDefinitionElement): String? {
            return element.getData<StellarisTechnologyData>()?.area
        }

        fun getCategories(element: ParadoxScriptDefinitionElement): Set<String> {
            return element.getData<StellarisTechnologyData>()?.category.orEmpty()
        }

        fun getAttributes(element: ParadoxScriptDefinitionElement): Set<String> {
            return element.definitionInfo?.let { getAttributes(it) }.orEmpty()
        }

        fun getAttributes(definitionInfo: ParadoxDefinitionInfo): Set<String> {
            return definitionInfo.getOrPutUserData(Keys.technologyAttributes) {
                definitionInfo.subtypeConfigs.filter { it.inGroup("technology_attribute") }.mapTo(mutableSetOf()) { it.name }
            }
        }

        /**
         * 得到指定科技的作为其前提条件的所有科技的名字。
         */
        fun getPrerequisites(definition: ParadoxScriptDefinitionElement): Set<String> {
            return CachedValuesManager.getCachedValue(definition, Keys.cachedPrerequisites) {
                val value = doGetPrerequisites(definition)
                CachedValueProvider.Result(value, definition)
            }
        }

        private fun doGetPrerequisites(definition: ParadoxScriptDefinitionElement): Set<String> {
            val data = ParadoxScriptDataResolver.resolve(definition) ?: return emptySet()
            val names: Set<String> by data.get("prerequisites", emptySet())
            return names
        }

        /**
         * 得到作为前提条件的科技列表。
         */
        fun getPreTechnologies(definition: ParadoxScriptDefinitionElement, selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): List<ParadoxScriptDefinitionElement> {
            //NOTE 1. 目前不兼容封装变量引用

            val name = definition.definitionInfo?.name
            val type = ParadoxDefinitionTypes.Technology
            if (name.isNullOrEmpty()) return emptyList()
            val prerequisites = getPrerequisites(definition)
            if (prerequisites.isEmpty()) return emptyList()
            selector.withGameType(gameType)
            return buildList b@{
                ParadoxDefinitionSearch.search(type, selector).processQuery p@{ rDefinition ->
                    ProgressManager.checkCanceled()
                    val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                    if (rDefinitionInfo.name.isEmpty()) return@p true
                    if (rDefinitionInfo.name !in prerequisites) return@p true
                    this += rDefinition
                    true
                }
            }
        }

        /**
         * 得到后续的科技列表。
         */
        fun getPostTechnologies(definition: ParadoxScriptDefinitionElement, selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): List<ParadoxScriptDefinitionElement> {
            //NOTE 1. 目前不兼容封装变量引用 2. 这里需要从所有同名定义查找使用

            val name = definition.definitionInfo?.name
            val type = ParadoxDefinitionTypes.Technology
            if (name.isNullOrEmpty()) return emptyList()
            selector.withGameType(gameType)
            return buildList b@{
                ParadoxDefinitionSearch.search(name, type, selector).processQuery p0@{ definition0 ->
                    ProgressManager.checkCanceled()
                    ReferencesSearch.search(definition0, selector.scope).processQuery p@{ ref ->
                        if (ref !is ParadoxScriptExpressionPsiReference) return@p true
                        val refElement = ref.element.castOrNull<ParadoxScriptString>() ?: return@p true
                        val rDefinition = refElement.findParentByPath("prerequisites/-", definitionType = type) ?: return@p true
                        val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                        if (rDefinitionInfo.name.isEmpty()) return@p true
                        if (rDefinitionInfo.type != type) return@p true
                        this += rDefinition
                        true
                    }
                    true
                }
            }.distinct()
        }
    }
}
