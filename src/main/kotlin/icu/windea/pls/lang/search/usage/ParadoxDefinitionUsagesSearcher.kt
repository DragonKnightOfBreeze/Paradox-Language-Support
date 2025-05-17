package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import kotlin.experimental.*

/**
 * 定义的使用的查询。
 *
 * * 定义对应的PsiElement的名字（rootKey）不一定是定义的名字（definitionName），需要特殊处理。
 */
class ParadoxDefinitionUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        if (target !is ParadoxScriptDefinitionElement) return
        val definitionInfo = runReadAction { target.definitionInfo }
        if (definitionInfo == null) return
        val definitionName = definitionInfo.name
        if (definitionName.isEmpty()) return //ignore anonymous definitions
        val type = definitionInfo.type
        val words = mutableSetOf<String>()
        words.add(definitionName)
        when {
            type == ParadoxDefinitionTypes.Sprite -> {
                val gfxName = definitionName.removePrefix("GFX_")
                if (gfxName.isNotNullOrEmpty()) words.add(gfxName)
                val gfxTextName = definitionName.removePrefix("GFX_text_")
                if (gfxTextName.isNotNullOrEmpty()) words.add(gfxTextName)
            }
            type == ParadoxDefinitionTypes.GameConcept -> {
                val data = target.getData<StellarisGameConceptData>()
                data?.alias?.forEach { words.add(it) }
            }
        }
        val ignoreCase = ParadoxIndexConstraint.Definition.entries.filter { it.ignoreCase }.any { it.predicate(type) }

        //这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_STRINGS or UsageSearchContext.IN_COMMENTS
        val processor = getProcessor(target)
        queryParameters.optimizer.wordRequests.removeIf { it.word in words }
        for (extraWord in words) {
            queryParameters.optimizer.searchWord(extraWord, useScope, searchContext, !ignoreCase, target, processor)
        }
    }

    private fun getProcessor(target: PsiElement): RequestResultProcessor {
        return ParadoxFilteredRequestResultProcessor(target, ParadoxResolveConstraint.Definition)
    }
}

