package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import icu.windea.pls.core.wordRequests
import icu.windea.pls.lang.search.ParadoxFilteredRequestResultProcessor
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import kotlin.experimental.or

/**
 * 本地化的使用的查询。
 */
class ParadoxLocalisationUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        if (target !is ParadoxLocalisationProperty) return
        val name = target.name
        if (name.isEmpty()) return
        val ignoreCase = ParadoxIndexConstraint.Localisation.entries.filter { it.ignoreCase }.any { it.supports(name) }

        //这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_COMMENTS
        val processor = getProcessor(target)
        queryParameters.optimizer.wordRequests.removeIf { it.word == name }
        queryParameters.optimizer.searchWord(name, useScope, searchContext, !ignoreCase, target, processor)
    }

    private fun getProcessor(target: PsiElement): RequestResultProcessor {
        return ParadoxFilteredRequestResultProcessor(target, ParadoxResolveConstraint.Localisation)
    }
}

