package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*

/**
 * 定义的使用的查询。
 *
 * 定义对应的PsiElement的名字（rootKey）不一定是定义的名字（definitionName），因此需要特殊处理。
 * 
 * 另外，某些场合引用处的文本并非完整的定义的名字，如本地化图标，这时也要特殊处理。
 * 
 * @see icu.windea.pls.script.intentions.DefinitionNameFindUsagesIntention
 */
class ParadoxDefinitionUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxDefinitionProperty) return
		val definitionInfo = runReadAction {  target.definitionInfo }
		if(definitionInfo == null) return
		val name = definitionInfo.name
		if(name.isEmpty()) return
		val type = definitionInfo.type
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			queryParameters.optimizer.searchWord(name, useScope, true, target)
			if(type == "sprite" || type == "spriteType") {
				queryParameters.optimizer.searchWord(name.removePrefix("GFX_"), useScope, true, target)
				queryParameters.optimizer.searchWord(name.removePrefix("GFX_text_"), useScope, true, target)
			}
		}
	}
}

