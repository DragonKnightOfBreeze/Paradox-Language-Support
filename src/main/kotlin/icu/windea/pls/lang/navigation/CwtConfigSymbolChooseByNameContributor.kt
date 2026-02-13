package icu.windea.pls.lang.navigation

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.core.getCurrentProject
import icu.windea.pls.core.process
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.psi.mock.CwtConfigSymbolNavigationElement
import icu.windea.pls.lang.search.CwtConfigSymbolSearch
import icu.windea.pls.lang.settings.PlsSettings

/**
 * 用于在 *随处搜索（Search Everywhere）* 中查找规则符号。
 *
 * 进行搜索时会尝试推断当前项目以及当前的游戏类型。
 */
class CwtConfigSymbolChooseByNameContributor : ChooseByNameContributorEx {
    // com.intellij.ide.util.gotoByName.CwtConfigSymbolChooseByNameContributor

    private fun getTypes(): Set<String> = buildSet {
        val settings = PlsSettings.getInstance().state.navigation
        if (settings.seForTypeConfigs) add(CwtConfigTypes.Type.id)
        if (settings.seForComplexEnumConfigs) add(CwtConfigTypes.ComplexEnum.id)
        if (settings.seForTriggerConfigs) add(CwtConfigTypes.Trigger.id)
        if (settings.seForEffectConfigs) add(CwtConfigTypes.Effect.id)
    }

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        val types = getTypes()
        if (types.isEmpty()) return
        val project = scope.project ?: getCurrentProject() ?: return
        val gameType = ParadoxAnalysisManager.getInferredCurrentGameType(project)
        CwtConfigSymbolSearch.search(null, types, gameType, project, scope).process p@{
            if (it.readWriteAccess != ReadWriteAccessDetector.Access.Write) return@p true // only accept declarations
            val name = it.name
            processor.process(name)
        }
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        val types = getTypes()
        if (types.isEmpty()) return
        val project = parameters.project
        val scope = parameters.searchScope
        val gameType = ParadoxAnalysisManager.getInferredCurrentGameType(project)
        CwtConfigSymbolSearch.search(null, types, gameType, project, scope).process p@{
            if (it.readWriteAccess != ReadWriteAccessDetector.Access.Write) return@p true // only accept declarations
            val name = it.name
            val configType = CwtConfigType.entries.get(it.type) ?: return@p true
            val element = it.element ?: return@p true
            val navigationElement = CwtConfigSymbolNavigationElement(element, name, configType, it.gameType, project)
            processor.process(navigationElement)
        }
    }
}
