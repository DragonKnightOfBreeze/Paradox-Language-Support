package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeGroup
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.process
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.ep.util.data.StellarisTechnologyData
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionName
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctBy
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.lang.util.data.get
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptString

@Suppress("unused")
object ParadoxTechnologyManager {
    object Keys : KeyRegistry() {
        val cachedPrerequisites by registerKey<CachedValue<Set<String>>>(Keys)
        val technologyAllAttributes by registerKey<Set<String>>(Keys)
        val technologyAttributes by registerKey<Set<String>>(Keys)
    }

    fun getTechnologies(selector: ParadoxSearchSelector<ParadoxDefinitionElement>): Set<ParadoxDefinitionElement> {
        return ParadoxDefinitionSearch.search(null, ParadoxDefinitionTypes.technology, selector).findAll()
    }

    fun getName(element: ParadoxDefinitionElement): String {
        return element.name // = element.definitionInfo.name
    }

    fun getLocalizedNameElement(definition: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    fun getIconFile(definition: ParadoxDefinitionElement): PsiFile? {
        return ParadoxDefinitionManager.getPrimaryImage(definition)
    }

    @WithGameType(ParadoxGameType.Stellaris)
    object Stellaris {
        private val gameType = ParadoxGameType.Stellaris

        fun getAllTiers(project: Project, context: Any?): Set<ParadoxDefinitionElement> {
            val selector = selector(project, context).definition().withGameType(gameType).contextSensitive().distinctBy { it.definitionName }
            return ParadoxDefinitionSearch.search(null, "technology_tier", selector).findAll()
        }

        fun getAllResearchAreas(): Set<String> {
            return PlsFacade.getConfigGroup(gameType).enums.get("research_area")?.values.orEmpty()
        }

        fun getAllResearchAreaConfigs(project: Project): Collection<CwtValueConfig> {
            return PlsFacade.getConfigGroup(project, gameType).enums.get("research_area")?.valueConfigMap?.values.orEmpty()
        }

        fun getAllCategories(project: Project, context: Any?): Set<ParadoxDefinitionElement> {
            val selector = selector(project, context).definition().withGameType(gameType).contextSensitive().distinctBy { it.definitionName }
            return ParadoxDefinitionSearch.search(null, ParadoxDefinitionTypes.technologyCategory, selector).findAll()
        }

        fun getAllAttributes(gameType: ParadoxGameType): Set<String> {
            val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.technology] ?: return emptySet()
            return eventConfig.config.getOrPutUserData(Keys.technologyAllAttributes) {
                eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.TechnologyAttribute }.map { it.name }.toSet()
            }
        }

        fun getAllAttributeConfigs(project: Project): Collection<CwtSubtypeConfig> {
            val eventConfig = PlsFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.technology] ?: return emptySet()
            return eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.TechnologyAttribute }
        }

        fun getTier(element: ParadoxDefinitionElement): String? {
            return element.getDefinitionData<StellarisTechnologyData>()?.tier
        }

        fun getArea(element: ParadoxDefinitionElement): String? {
            return element.getDefinitionData<StellarisTechnologyData>()?.area
        }

        fun getCategories(element: ParadoxDefinitionElement): Set<String> {
            return element.getDefinitionData<StellarisTechnologyData>()?.category.orEmpty()
        }

        fun getAttributes(element: ParadoxDefinitionElement): Set<String> {
            return element.definitionInfo?.let { getAttributes(it) }.orEmpty()
        }

        fun getAttributes(definitionInfo: ParadoxDefinitionInfo): Set<String> {
            return definitionInfo.getOrPutUserData(Keys.technologyAttributes) {
                definitionInfo.subtypeConfigs.filter { it in CwtSubtypeGroup.TechnologyAttribute }.mapTo(mutableSetOf()) { it.name }
            }
        }

        /**
         * 得到指定科技的作为其前提条件的所有科技的名字。
         */
        fun getPrerequisites(definition: ParadoxDefinitionElement): Set<String> {
            return CachedValuesManager.getCachedValue(definition, Keys.cachedPrerequisites) {
                val value = doGetPrerequisites(definition)
                CachedValueProvider.Result(value, definition)
            }
        }

        private fun doGetPrerequisites(definition: ParadoxDefinitionElement): Set<String> {
            val data = ParadoxScriptDataResolver.DEFAULT.resolve(definition) ?: return emptySet()
            val names: Set<String> by data.get("prerequisites", emptySet())
            return names
        }

        /**
         * 得到作为前提条件的科技列表。
         */
        fun getPreTechnologies(definition: ParadoxDefinitionElement, selector: ParadoxSearchSelector<ParadoxDefinitionElement>): List<ParadoxDefinitionElement> {
            // NOTE 1. 目前不兼容封装变量引用

            val name = definition.definitionInfo?.name
            val type = ParadoxDefinitionTypes.technology
            if (name.isNullOrEmpty()) return emptyList()
            val prerequisites = getPrerequisites(definition)
            if (prerequisites.isEmpty()) return emptyList()
            selector.withGameType(gameType)
            return buildList b@{
                ParadoxDefinitionSearch.search(null, type, selector).process p@{ rDefinition ->
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
        fun getPostTechnologies(definition: ParadoxDefinitionElement, selector: ParadoxSearchSelector<ParadoxDefinitionElement>): List<ParadoxDefinitionElement> {
            // NOTE 1. 目前不兼容封装变量引用 2. 这里需要从所有同名定义查找用法

            val name = definition.definitionInfo?.name
            val type = ParadoxDefinitionTypes.technology
            if (name.isNullOrEmpty()) return emptyList()
            selector.withGameType(gameType)
            return buildList b@{
                ParadoxDefinitionSearch.search(name, type, selector).process p0@{ definition0 ->
                    ProgressManager.checkCanceled()
                    ReferencesSearch.search(definition0, selector.scope).process p@{ ref ->
                        if (ref !is ParadoxScriptExpressionPsiReference) return@p true
                        val refElement = ref.element.castOrNull<ParadoxScriptString>() ?: return@p true
                        val rDefinition = selectScope { refElement.parentOfPath("prerequisites/-", definitionType = type).asProperty() } ?: return@p true
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
