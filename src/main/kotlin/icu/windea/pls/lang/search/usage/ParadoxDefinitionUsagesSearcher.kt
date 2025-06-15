package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.ep.icon.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.model.*
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
        if (definitionInfo.name.isEmpty()) return //ignore anonymous definitions
        val words = getWords(target, definitionInfo)
        val ignoreCase = ParadoxIndexConstraint.Definition.entries.filter { it.ignoreCase }.any { it.predicate(definitionInfo.type) }

        //这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_STRINGS or UsageSearchContext.IN_COMMENTS
        val processor = getProcessor(target)
        queryParameters.optimizer.wordRequests.removeIf { it.word in words }
        for (word in words) {
            queryParameters.optimizer.searchWord(word, useScope, searchContext, !ignoreCase, target, processor)
        }
    }

    private fun getWords(target: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val words = mutableSetOf<String>()
        words.add(definitionInfo.name)

        // for <game_concept>
        if (definitionInfo.type == ParadoxDefinitionTypes.GameConcept) {
            val data = target.getData<StellarisGameConceptData>()
            data?.alias?.forEach {
                val name = it.orNull()
                if (name != null) words.add(name)
            }
        }

        // from localisation icons
        val nameGetters = mutableSetOf<(String) -> String?>()
        ParadoxLocalisationIconSupport.EP_NAME.extensionList.forEach { support ->
            addToNameGetters(support, definitionInfo, nameGetters)
        }
        nameGetters.forEach { nameGetter ->
            val name = nameGetter(definitionInfo.name)?.orNull()
            if (name != null) words.add(name)
        }

        return words
    }

    private fun addToNameGetters(support: ParadoxLocalisationIconSupport, definitionInfo: ParadoxDefinitionInfo, nameGetters: MutableSet<(String) -> String?>) {
        when (support) {
            is CompositeParadoxLocalisationIconSupport -> {
                support.supports.forEach { s -> addToNameGetters(s, definitionInfo, nameGetters) }
            }
            is DefinitionBasedParadoxLocalisationIconSupport -> {
                if (ParadoxDefinitionTypeExpression.resolve(support.definitionType).matches(definitionInfo)) {
                    nameGetters.add(support.nameGetter)
                }
            }
        }
    }

    private fun getProcessor(target: PsiElement): RequestResultProcessor {
        return ParadoxFilteredRequestResultProcessor(target, ParadoxResolveConstraint.Definition)
    }
}

