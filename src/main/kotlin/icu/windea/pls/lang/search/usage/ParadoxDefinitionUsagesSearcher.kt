package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.wordRequests
import icu.windea.pls.ep.data.StellarisGameConceptData
import icu.windea.pls.ep.icon.CompositeParadoxLocalisationIconSupport
import icu.windea.pls.ep.icon.DefinitionBasedParadoxLocalisationIconSupport
import icu.windea.pls.ep.icon.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.getData
import icu.windea.pls.lang.search.ParadoxFilteredRequestResultProcessor
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import kotlin.experimental.or

/**
 * 定义的使用的查询。
 *
 * * 定义对应的 PSI（[ParadoxScriptDefinitionElement]） 的名字（rootKey）不一定是定义的名字（definitionName），需要特殊处理。
 */
class ParadoxDefinitionUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        // NOTE SUFFIX_AWARE 不兼容需要带上后缀的情况，目前不支持

        val target = queryParameters.elementToSearch
        if (target !is ParadoxScriptDefinitionElement) return
        val definitionInfo = runReadAction { target.definitionInfo }
        if (definitionInfo == null) return
        if (definitionInfo.name.isEmpty()) return //ignore anonymous definitions
        val words = getWords(target, definitionInfo)
        val ignoreCase = ParadoxIndexConstraint.Definition.entries.filter { it.ignoreCase }.any { it.supports(definitionInfo.type) }

        // 这里不能直接使用target.useScope，否则文件高亮会出现问题
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

