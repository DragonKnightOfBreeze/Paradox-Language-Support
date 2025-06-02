package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.data.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

object ParadoxTechnologyManager {
    object Keys : KeyRegistry() {
        val cachedPrerequisites by createKey<CachedValue<Set<String>>>(this)
        val technologyAllAttributes by createKey<Set<String>>(this)
        val technologyAttributes by createKey<Set<String>>(this)
    }

    fun getTechnologies(selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): Set<ParadoxScriptDefinitionElement> {
        return ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.Technology, selector).findAll()
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
