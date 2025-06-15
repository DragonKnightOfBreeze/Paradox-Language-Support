package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

/**
 * CWT规则的查询。
 */
class CwtConfigUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    //* CWT规则的属性名为"alias[x:y]"时，其在脚本文件中匹配的属性名会是"y"，需要特殊处理。
    //* CWT规则的属性名为"inline[x]"时，其在脚本文件中匹配的属性名会是"x"，需要特殊处理。
    //* 对于CWT连接规则，其在脚本文件中可能匹配其前缀，需要特殊处理。

    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        if (target !is CwtProperty) return
        val extraWords = getExtraWords(target)
        if (extraWords.isEmpty()) return

        //这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE
        for (extraWord in extraWords) {
            queryParameters.optimizer.searchWord(extraWord, useScope, searchContext, false, target) //忽略大小写
        }
    }

    private fun getExtraWords(target: CwtProperty): Set<String> {
        val extraWords = mutableSetOf<String>()
        val configType = CwtConfigManager.getConfigType(target)
        when (configType) {
            CwtConfigTypes.Alias, CwtConfigTypes.Modifier, CwtConfigTypes.Trigger, CwtConfigTypes.Effect -> {
                val aliasSubName = target.name.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")?.orNull()
                if (aliasSubName != null) extraWords.add(aliasSubName)
            }
            CwtConfigTypes.Inline -> {
                val inlineName = target.name.removeSurroundingOrNull("inline[", "]")?.orNull()
                if (inlineName != null) extraWords.add(inlineName)
            }
            CwtConfigTypes.Link -> {
                val prefixProperty = target.propertyValue?.castOrNull<CwtBlock>()?.findChild<CwtProperty> { it.name == "prefix" }
                val prefix = prefixProperty?.propertyValue?.castOrNull<CwtString>()?.stringValue?.orNull()
                if (prefix != null) extraWords.add(prefix)
            }
            else -> pass()
        }
        return extraWords
    }
}
