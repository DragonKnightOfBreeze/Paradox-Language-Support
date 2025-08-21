package icu.windea.pls.lang.navigation

import com.intellij.navigation.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于在 *随处搜索（Search Everywhere）* 中查找本地化。
 */
class ParadoxLocalisationChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    private val indexKey = ParadoxIndexKeys.LocalisationName

    private fun isEnabled() = PlsFacade.getSettings().navigation.seForLocalisations

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
