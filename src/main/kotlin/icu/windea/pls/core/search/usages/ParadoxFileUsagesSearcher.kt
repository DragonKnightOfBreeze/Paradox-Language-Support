package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*

/**
 * 文件（相对于游戏或模组根目录）的使用的查询。
 */
class ParadoxFileUsagesSearcher: QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		//TODO 0.7.8 这里不能仅仅用fileName去查找，需要基于CWT规则文件判断
		val target = queryParameters.elementToSearch
		if(target !is PsiFile) return
		val fileInfo = target.fileInfo
		if(fileInfo == null) return
		val name = target.name
		val nameWithoutExtension = name.substringBeforeLast('.', "")
		if(nameWithoutExtension.isEmpty()) return
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			queryParameters.optimizer.searchWord(nameWithoutExtension, useScope, true, target)
		}
	}
}