package icu.windea.pls.lang.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import icu.windea.pls.lang.search.ParadoxTextBasedTargetSearch

/**
 * 用于在 *随处搜索（Search Everywhere）* 中基于本地化文本片段查找各种目标（封装变量/定义/本地化）。
 */
class ParadoxTextBasedTargetChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        // 这里什么也不用写
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        // 由具体的查询器实现决定是否选择性地启用
        val project = parameters.project
        val scope = parameters.searchScope
        ParadoxTextBasedTargetSearch.search(name, project, scope).forEach {
            if (it is NavigationItem) processor.process(it)
        }
    }
}
