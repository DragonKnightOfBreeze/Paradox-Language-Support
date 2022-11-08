package icu.windea.pls.core.search.references

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*

/**
 * 文件路径的使用的查询。
 *
 * 对于同一文件路径对应的文件，其查找使用的结果应当是一致的。
 */
class ParadoxFilePathReferencesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is PsiFile) return
		val filePath = target.fileInfo?.path?.toString() ?: return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			queryParameters.optimizer.searchWord(filePath, target.useScope, true, target)
		}
	}
}
