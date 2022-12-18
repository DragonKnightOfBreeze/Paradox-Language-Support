package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 定义的使用的查询。
 *
 * * 定义对应的PsiElement的名字（rootKey）不一定是定义的名字（definitionName），需要特殊处理。
 * 
 * 另外，某些场合引用处的文本并非完整的定义的名字，如本地化图标，这时也要特殊处理。
 * 
 * @see icu.windea.pls.script.intentions.DefinitionNameFindUsagesIntention
 */
class ParadoxDefinitionUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxScriptDefinitionElement) return
		val definitionInfo = runReadAction {  target.definitionInfo }
		if(definitionInfo == null) return
		val definitionName = definitionInfo.name
		if(definitionName.isEmpty()) return
		val type = definitionInfo.type
		val project = queryParameters.project
		val configGroup = definitionInfo.configGroup
		val extraWords = SmartList<String>()
		if(definitionInfo.rootKey != definitionName) {
			extraWords.add(definitionName)
		}
		if(type == "sprite" || type == "spriteType") {
			val gfxTextName = definitionName.removePrefix("GFX_text_")
			if(gfxTextName.isNotEmpty()) {
				extraWords.add(gfxTextName)
			} else {
				val gfxName = definitionName.removePrefix("GFX_")
				if(gfxTextName.isNotEmpty()) {
					extraWords.add(gfxName)
				}
			}
		}
		configGroup.info.typeExpressionStringLinks
			.firstNotNullOfOrNull { it.extract(definitionName) }
			?.takeIfNotEmpty()
			?.let { extraWords.add(it) }
		if(extraWords.isEmpty()) return
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			for(extraWord in extraWords) {
				queryParameters.optimizer.searchWord(extraWord, useScope, true, target)
			}
		}
	}
}

