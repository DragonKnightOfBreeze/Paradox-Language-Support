package icu.windea.pls.localisation.navigation

import com.intellij.navigation.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于在随处搜索（Search Everywhere）中查找对应名字的同步本地化。
 *
 * 如果启用了对应的配置，也可以查找对应本地化文本（移除了大部分特殊格式）的本地化。
 *
 * @see icu.windea.pls.lang.settings.PlsSettingsState.OthersState.searchEverywhereByLocalisationText
 * @see icu.windea.pls.PlsConstants.Settings.maxLocalisationTextLengthToIndex
 */
class ParadoxSyncedLocalisationChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    private val indexKey = ParadoxIndexManager.SyncedLocalisationNameKey
    private val indexKeyByText = ParadoxIndexManager.LocalisationTextKey

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        StubIndex.getInstance().processAllKeys(indexKey, processor, scope, filter)
        if (PlsFacade.getSettings().others.searchEverywhereByLocalisationText) {
            StubIndex.getInstance().processAllKeys(indexKeyByText, processor, scope, filter)
        }
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        val project = parameters.project
        val scope = parameters.searchScope
        val idFilter = parameters.idFilter
        val requiredClass = ParadoxLocalisationProperty::class.java
        StubIndex.getInstance().processElements(indexKey, name, project, scope, idFilter, requiredClass, processor)
        if (PlsFacade.getSettings().others.searchEverywhereByLocalisationText) {
            StubIndex.getInstance().processElements(indexKeyByText, name, project, scope, idFilter, requiredClass, processor)
        }
    }
}
