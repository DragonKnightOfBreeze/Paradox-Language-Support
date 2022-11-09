package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*

/**
 * 文件路径的使用的查询。
 */
class ParadoxFilePathUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is PsiFile) return
		val filePath = target.fileInfo?.path?.toString() ?: return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val useScope = getUseScope(queryParameters)
			queryParameters.optimizer.searchWord(filePath, useScope, true, target)
		}
	}
}
