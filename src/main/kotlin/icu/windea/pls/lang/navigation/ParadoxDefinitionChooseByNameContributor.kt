package icu.windea.pls.lang.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopeUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import icu.windea.pls.core.getCurrentProject
import icu.windea.pls.core.process
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.settings.PlsSettings

/**
 * 用于在 *随处搜索（Search Everywhere）* 中查找定义。
 */
class ParadoxDefinitionChooseByNameContributor : ChooseByNameContributorEx {
    // com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    private fun isEnabled() = PlsSettings.getInstance().state.navigation.seForDefinitions

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        if (!isEnabled()) return
        val project = scope.project ?: getCurrentProject() ?: return
        val gameType = ParadoxAnalysisManager.getInferredCurrentGameType(project)
        val selector = selector(project).definition().withSearchScope(scope).withGameType(gameType)
        ParadoxDefinitionSearch.search(null, null, selector).process p@{
            val name = it.name
            processor.process(name)
        }
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        if (!isEnabled()) return
        val project = parameters.project
        val scope = GlobalSearchScopeUtil.toGlobalSearchScope(parameters.searchScope, project)
        val gameType = ParadoxAnalysisManager.getInferredCurrentGameType(project)
        val selector = selector(project).definition().withSearchScope(scope).withGameType(gameType)
        ParadoxDefinitionSearch.searchElement(name, null, selector).process(processor)
    }
}
