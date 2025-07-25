@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.util.Query

//org.intellij.plugins.markdown.model.psi.MarkdownSymbolUsageSearcher

class CwtConfigSymbolUsageSearcher: UsageSearcher {
    override fun collectSearchRequests(parameters: UsageSearchParameters): Collection<@JvmWildcard Query<out Usage>> {
        val target = parameters.target
        if(target !is CwtConfigSymbol) return emptySet()
        return CwtConfigSymbolManager.buildSearchRequests(parameters.project, parameters.searchScope, target)
    }
}
