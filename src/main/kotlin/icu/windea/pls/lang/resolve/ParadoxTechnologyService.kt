package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.searches.ReferencesSearch
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimizedIfEmpty
import icu.windea.pls.core.process
import icu.windea.pls.lang.data.ParadoxScriptDataResolver
import icu.windea.pls.lang.data.get
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.withGameType
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxTechnologyManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString

object ParadoxTechnologyService {
    @ForGameType(ParadoxGameType.Stellaris)
    object Stellaris {
        private val gameType = ParadoxGameType.Stellaris

        fun resolvePrerequisites(definition: ParadoxDefinitionElement): Set<String> {
            val data = ParadoxScriptDataResolver.DEFAULT.resolve(definition) ?: return emptySet()
            val result: Set<String> by data.get("prerequisites", emptySet())
            return result.optimizedIfEmpty()
        }

        /**
         * 得到作为前提条件的科技列表。
         */
        fun resolvePreTechnologies(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
            // NOTE 1. 目前不兼容封装变量引用

            val name = definition.definitionInfo?.name
            val type = ParadoxDefinitionTypes.technology
            if (name.isNullOrEmpty()) return emptyList()
            val prerequisites = ParadoxTechnologyManager.Stellaris.getPrerequisites(definition)
            if (prerequisites.isEmpty()) return emptyList()
            selector.withGameType(gameType)
            val result = buildList b@{
                ParadoxDefinitionSearch.searchProperty(null, type, selector).process p@{ rDefinition ->
                    ProgressManager.checkCanceled()
                    val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                    if (rDefinitionInfo.name.isEmpty()) return@p true
                    if (rDefinitionInfo.name !in prerequisites) return@p true
                    this += rDefinition
                    true
                }
            }.distinct()
            return result.optimizedIfEmpty()
        }

        /**
         * 得到后续的科技列表。
         */
        fun resolvePostTechnologies(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
            // NOTE 1. 目前不兼容封装变量引用 2. 这里需要从所有同名定义查找用法

            val name = definition.definitionInfo?.name
            val type = ParadoxDefinitionTypes.technology
            if (name.isNullOrEmpty()) return emptyList()
            selector.withGameType(gameType)
            val result = buildList b@{
                ParadoxDefinitionSearch.searchProperty(name, type, selector).process p0@{ definition0 ->
                    ProgressManager.checkCanceled()
                    ReferencesSearch.search(definition0, selector.scope).process p@{ ref ->
                        ProgressManager.checkCanceled()
                        if (ref !is ParadoxScriptExpressionPsiReference) return@p true
                        val refElement = ref.element.castOrNull<ParadoxScriptString>() ?: return@p true
                        val refDefinition = selectScope { refElement.queryParentBy("*/prerequisites/-").asProperty() } ?: return@p true
                        val refDefinitionInfo = refDefinition.definitionInfo ?: return@p true
                        if (refDefinitionInfo.name.isEmpty()) return@p true
                        if (refDefinitionInfo.type != type) return@p true
                        this += refDefinition
                        true
                    }
                    true
                }
            }.distinct()
            return result.optimizedIfEmpty()
        }
    }
}
