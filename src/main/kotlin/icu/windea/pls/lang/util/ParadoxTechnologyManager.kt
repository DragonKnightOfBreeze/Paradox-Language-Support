package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.data.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

object ParadoxTechnologyManager {
    object Keys : KeyRegistry() {
        val cachedPrerequisites by createKey<CachedValue<Set<String>>>(this)
    }

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
        fun getTiers(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
            val selector = selector(project, context).definition().withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
            return ParadoxDefinitionSearch.search("technology_tier", selector).findAll()
        }

        fun getResearchAreas(): Set<String> {
            return getConfigGroup(ParadoxGameType.Stellaris).enums.get("research_area")?.values.orEmpty()
        }

        fun getCategories(project: Project, context: Any?): Set<ParadoxScriptDefinitionElement> {
            val selector = selector(project, context).definition().withGameType(ParadoxGameType.Stellaris).contextSensitive().distinctByName()
            return ParadoxDefinitionSearch.search("technology_category", selector).findAll()
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
            if (name.isNullOrEmpty()) return emptyList()
            val prerequisites = getPrerequisites(definition)
            if (prerequisites.isEmpty()) return emptyList()
            selector.withGameType(ParadoxGameType.Stellaris)
            return buildList b@{
                ParadoxDefinitionSearch.search("technology", selector).processQuery p@{ rDefinition ->
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
            if (name.isNullOrEmpty()) return emptyList()
            selector.withGameType(ParadoxGameType.Stellaris)
            return buildList b@{
                ParadoxDefinitionSearch.search(name, "technology", selector).processQuery p0@{ definition0 ->
                    ProgressManager.checkCanceled()
                    ReferencesSearch.search(definition0, selector.scope).processQuery p@{ ref ->
                        if (ref !is ParadoxScriptExpressionPsiReference) return@p true
                        val refElement = ref.element.castOrNull<ParadoxScriptString>() ?: return@p true
                        val rDefinition = refElement.findParentByPath("prerequisites/-", definitionType = "technology") ?: return@p true
                        val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                        if (rDefinitionInfo.name.isEmpty()) return@p true
                        if (rDefinitionInfo.type != "technology") return@p true
                        this += rDefinition
                        true
                    }
                    true
                }
            }.distinct()
        }
    }
}
