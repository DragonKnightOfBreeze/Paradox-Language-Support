package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.stringValue

/**
 * 规则的用法的查询。
 */
class CwtConfigUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    // - 规则的属性名为 `alias[x:y]` 时，其在脚本文件中匹配的属性名会是 `y`，需要特殊处理
    // - 规则的属性名为 `directive[x]` 时，其在脚本文件中匹配的属性名可能会是 `x`，需要特殊处理
    // - 对于连接规则，其在脚本文件中可能匹配其前缀，需要特殊处理

    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        if (target !is CwtProperty) return

        val extraWords = getExtraWords(target)
        if (extraWords.isEmpty()) return

        // 这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE
        for (extraWord in extraWords) {
            queryParameters.optimizer.searchWord(extraWord, useScope, searchContext, false, target) // 忽略大小写
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
            CwtConfigTypes.Directive -> {
                val inlineName = target.name.removeSurroundingOrNull("directive[", "]")?.orNull()
                if (inlineName != null) extraWords.add(inlineName)
            }
            CwtConfigTypes.Link, CwtConfigTypes.LocalisationLink -> {
                val prefixProperty = target.propertyValue?.castOrNull<CwtBlock>()?.findChild<CwtProperty> { it.name == "prefix" }
                val prefix = prefixProperty?.propertyValue?.castOrNull<CwtString>()?.stringValue?.orNull()
                if (prefix != null) extraWords.add(prefix)
            }
            else -> pass()
        }
        return extraWords
    }
}
