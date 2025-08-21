package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.config.util.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.search.scope.*

/**
 * CWT规则符号的查询。
 */
class CwtConfigSymbolUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        if (target !is CwtProperty && target !is CwtStringExpressionElement) return

        val extraWords = getExtraWords(target)
        if (extraWords.isEmpty()) return

        //这里不能直接使用target.useScope，否则文件高亮会出现问题
        //只需要在CWT文件中查询
        val useScope = queryParameters.effectiveSearchScope
            .let { GlobalSearchScopeUtil.toGlobalSearchScope(it, queryParameters.project).withFileTypes(CwtFileType) }
        val searchContext = UsageSearchContext.IN_CODE
        for (extraWord in extraWords) {
            queryParameters.optimizer.searchWord(extraWord, useScope, searchContext, true, target)
        }
    }

    private fun getExtraWords(target: PsiElement): Set<String> {
        if (target is CwtProperty) return getExtraWords(target.propertyKey)
        if (target !is CwtStringExpressionElement) return emptySet()
        val extraWords = mutableSetOf<String>()
        val infos = CwtConfigSymbolManager.getInfos(target)
        infos.forEach { info -> extraWords.add(info.name) }
        return extraWords
    }
}
