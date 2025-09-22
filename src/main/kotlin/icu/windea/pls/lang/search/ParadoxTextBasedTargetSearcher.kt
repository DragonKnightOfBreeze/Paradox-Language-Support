package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.util.Processor

/**
 * 基于本地化文本片段的目标（封装变量/定义/本地化）查询器。
 */
class ParadoxTextBasedTargetSearcher: QueryExecutorBase<PsiElement, ParadoxTextBasedTargetSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxTextBasedTargetSearch.SearchParameters, consumer: Processor<in PsiElement>) {
        // TODO 2.0.5
    }
}
