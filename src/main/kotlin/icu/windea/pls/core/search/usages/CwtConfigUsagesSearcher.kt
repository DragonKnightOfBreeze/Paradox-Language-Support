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
 * CWT规则的查询。
 * 
 * * CWT别名规则对应的属性名是"alias[x:y]"，而脚本文件中对应的属性名是"y"，需要特殊处理。
 * * CWT连接规则的属性名是"script_value"，而脚本文件中可能会使用其前缀"value:"，需要特殊处理。
 */
class CwtConfigUsagesSearcher: QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is CwtProperty) return
		val extraWords = SmartList<String>()
		val configType = CwtConfigType.resolve(target)
		when(configType) {
			CwtConfigType.Alias -> {
				val aliasSubName = target.name.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
				if(!aliasSubName.isNullOrEmpty()) extraWords.add(aliasSubName)
			}
			CwtConfigType.Link -> {
				val prefixProperty = target.findChildOfType<CwtProperty> { it.name == "prefix" }
				val prefix = prefixProperty?.propertyValue?.castOrNull<CwtString>()?.stringValue
				if(!prefix.isNullOrEmpty()) extraWords.add(prefix)
			}
			else -> return
		}
		if(extraWords.isEmpty()) return
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			for(extraWord in extraWords) {
				queryParameters.optimizer.searchWord(extraWord, useScope, true, target)
			}
		}
	}
}