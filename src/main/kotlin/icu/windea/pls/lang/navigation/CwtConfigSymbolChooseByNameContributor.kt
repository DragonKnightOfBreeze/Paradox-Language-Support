package icu.windea.pls.lang.navigation

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.core.getCurrentProject
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.lang.psi.mock.CwtConfigSymbolNavigationElement
import icu.windea.pls.lang.search.CwtConfigSymbolSearch
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于在 *随处搜索（Search Everywhere）* 中查找CWT规则符号。
 *
 * 进行搜索时会尝试推断当前项目以及当前的游戏类型。
 */
class CwtConfigSymbolChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.CwtConfigSymbolChooseByNameContributor

    private fun getTypes(): Set<String> = buildSet {
        val settings = PlsFacade.getSettings().navigation
        if (settings.seForCwtTypeConfigs) add(CwtConfigTypes.Type.id)
        if (settings.seForCwtComplexEnumConfigs) add(CwtConfigTypes.ComplexEnum.id)
        if (settings.seForCwtTriggerConfigs) add(CwtConfigTypes.Trigger.id)
        if (settings.seForCwtEffectConfigs) add(CwtConfigTypes.Effect.id)
    }

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        val types = getTypes()
        if (types.isEmpty()) return
        val project = scope.project ?: getCurrentProject() ?: return
        val gameType = getInferredCurrentGameType(project)
        CwtConfigSymbolSearch.search(null, types, gameType, project, scope).forEach f@{
            if (it.readWriteAccess != ReadWriteAccessDetector.Access.Write) return@f // only accept declarations
            val name = it.name
            processor.process(name)
        }
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        val types = getTypes()
        if (types.isEmpty()) return
        val project = parameters.project
        val scope = parameters.searchScope
        val gameType = getInferredCurrentGameType(project)
        CwtConfigSymbolSearch.search(null, types, gameType, project, scope).forEach f@{
            if (it.readWriteAccess != ReadWriteAccessDetector.Access.Write) return@f // only accept declarations
            val expressionElement = it.virtualFile?.toPsiFile(project)?.findElementAt(it.elementOffset)?.parentOfType<CwtStringExpressionElement>() ?: return@f
            val name = it.name
            val configType = CwtConfigType.entries.get(it.type) ?: return@f
            val element = CwtConfigSymbolNavigationElement(expressionElement, name, configType, it.gameType, project)
            processor.process(element)
        }
    }

    private fun getInferredCurrentGameType(project: Project): ParadoxGameType? {
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.selectedEditor?.let { selectGameType(it.file) }?.let { return it }
        fileEditorManager.selectedEditors.firstNotNullOfOrNull { selectGameType(it.file) }?.let { return it }
        fileEditorManager.allEditors.firstNotNullOfOrNull { selectGameType(it.file) }?.let { return it }
        return null
    }
}
