package icu.windea.pls.lang.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 用于在 *随处搜索（Search Everywhere）* 中查找同步本地化。
 */
class ParadoxSyncedLocalisationChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    private val indexKey = ParadoxIndexKeys.SyncedLocalisationName

    private fun isEnabled() = PlsFacade.getSettings().navigation.seForSyncedLocalisations

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        if (!isEnabled()) return
        StubIndex.getInstance().processAllKeys(indexKey, processor, scope, filter)
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        if (!isEnabled()) return
        val project = parameters.project
        val scope = parameters.searchScope
        val idFilter = parameters.idFilter
        val requiredClass = ParadoxLocalisationProperty::class.java
        StubIndex.getInstance().processElements(indexKey, name, project, scope, idFilter, requiredClass, processor)
    }
}
