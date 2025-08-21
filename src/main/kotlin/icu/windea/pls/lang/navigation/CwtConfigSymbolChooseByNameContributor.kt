package icu.windea.pls.lang.navigation

import com.intellij.codeInsight.highlighting.*
import com.intellij.navigation.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.model.*

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
