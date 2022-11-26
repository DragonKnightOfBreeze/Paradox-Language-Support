package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*

/**
 * CWT别名规则的查询。
 * 
 * CWT规则文件中CWT别名规则对应的属性的名字是"alias[x:y]"，而脚本文件中对应的属性的名字是"y"，因此需要特殊处理。
 * 
 * 不允许直接从CWT属性查找使用（如，脚本文件中对应的属性）。这里用于，如，鼠标放到脚本属性上时显示的引用高亮。
 */
class CwtAliasUsagesSearcher: QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is CwtProperty) return
		if(CwtConfigType.resolve(target) != CwtConfigType.Alias) return
		val name = target.name.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
		if(name == null || name.isEmpty()) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			queryParameters.optimizer.searchWord(name, useScope, true, target)
		}
	}
}
