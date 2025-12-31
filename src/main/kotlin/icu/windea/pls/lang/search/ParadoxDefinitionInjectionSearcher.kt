package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义注入的查询器。
 */
class ParadoxDefinitionInjectionSearcher : QueryExecutorBase<ParadoxScriptProperty, ParadoxDefinitionInjectionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val mode = queryParameters.mode
        val targetKey = queryParameters.targetKey
        processQUeryFOrDefinitionInjections(mode, targetKey, project, scope) { element -> consumer.process(element) }
    }

    private fun processQUeryFOrDefinitionInjections(
        mode: String?,
        targetKey: String?,
        project: Project,
        scope: GlobalSearchScope,
        processor: Processor<ParadoxScriptProperty>
    ): Boolean {
        val indexKey = PlsIndexKeys.DefinitionInjectionTarget
        if (targetKey == null) {
            PlsIndexService.processElementsByKeys(indexKey, project, scope) p@{ _, element ->
                if (!matchesMode(element, mode)) return@p true
                processor.process(element)
            }
        } else {
            PlsIndexService.processElements(indexKey, targetKey, project, scope) p@{ element ->
                if (!matchesMode(element, mode)) return@p true
                processor.process(element)
            }
        }
        return true
    }

    private fun matchesMode(element: ParadoxScriptProperty, mode: String?): Boolean {
        if (mode == null) return true
        val actualMode = ParadoxDefinitionInjectionManager.getModeFromExpression(element.name)
        return actualMode.equals(mode, true)
    }
}
