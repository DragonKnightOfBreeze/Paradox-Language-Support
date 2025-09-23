package icu.windea.pls.lang.navigation

import com.intellij.navigation.ChooseByNameContributorEx2
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import icu.windea.pls.core.processQuery
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.search.target.ParadoxTextBasedTargetSearch
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.codeInsight.ParadoxTargetInfo

/**
 * 用于在 *随处搜索（Search Everywhere）* 中基于本地化文本片段查找各种目标（封装变量/定义/本地化）。
 */
class ParadoxTextBasedTargetChooseByNameContributor : ChooseByNameContributorEx2 {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    override fun processNames(processor: Processor<in String>, parameters: FindSymbolParameters) {
        // 从模式获取锚点
        val name = parameters.localPatternName.trim()
        val project = parameters.project
        val scope = parameters.searchScope
        if (name.isEmpty()) return // 预先过滤
        ParadoxTextBasedTargetSearch.search(name, project, scope).forEach { targetInfo ->
            processor.process(ParadoxTargetInfo.getAnchor(targetInfo))
        }
    }

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        // 这里什么也不用写
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        // 从锚点获取导航目标
        // 由具体的查询器实现决定是否选择性地启用
        val project = parameters.project
        val scope = parameters.searchScope
        val targetInfo = ParadoxTargetInfo.fromAnchor(name) ?: return
        when (targetInfo) {
            is ParadoxTargetInfo.ScriptedVariable -> {
                val query = ParadoxScriptedVariableSearch.search(targetInfo.name, selector(project).scriptedVariable().withSearchScope(scope))
                query.processQuery { processor.process(it) }
            }
            is ParadoxTargetInfo.Definition -> {
                val query = ParadoxDefinitionSearch.search(targetInfo.name, targetInfo.type, selector(project).definition().withSearchScope(scope))
                query.processQuery { processor.process(it) }
            }
            is ParadoxTargetInfo.Localisation -> {
                val query = when (targetInfo.type) {
                    ParadoxLocalisationType.Normal -> ParadoxLocalisationSearch.search(targetInfo.name, selector(project).localisation().withSearchScope(scope))
                    ParadoxLocalisationType.Synced -> ParadoxSyncedLocalisationSearch.search(targetInfo.name, selector(project).localisation().withSearchScope(scope))
                }
                query.processQuery { processor.process(it) }
            }
            else -> {}
        }
    }
}
