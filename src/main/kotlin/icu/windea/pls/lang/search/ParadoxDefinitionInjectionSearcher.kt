package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxDefinitionInjectionIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 定义注入的查询器。
 */
class ParadoxDefinitionInjectionSearcher : QueryExecutorBase<ParadoxDefinitionInjectionIndexInfo, ParadoxDefinitionInjectionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters, consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val mode = queryParameters.mode
        val target = queryParameters.target
        val type = queryParameters.type
        val keys = buildSet {
            // injected file 必须依赖 LazyIndexKey 才能进入候选集（IndexInfoAwareFileBasedIndex 的 lazy 实现）
            add(ParadoxDefinitionInjectionIndex.LazyIndexKey)

            when {
                !type.isNullOrEmpty() && !target.isNullOrEmpty() -> add(type + "@" + target)
                !type.isNullOrEmpty() -> add(ParadoxDefinitionInjectionIndex.typeIndexKey(type))
                !target.isNullOrEmpty() -> add(ParadoxDefinitionInjectionIndex.targetIndexKey(target))
                else -> add(ParadoxDefinitionInjectionIndex.AllIndexKey)
            }
        }
        PlsIndexService.processAllFileData(ParadoxDefinitionInjectionIndex::class.java, keys, project, scope, queryParameters.gameType) p@{ file, fileData ->
            val infos = when {
                !type.isNullOrEmpty() && !target.isNullOrEmpty() -> fileData[type + "@" + target].orEmpty()
                !type.isNullOrEmpty() -> fileData[ParadoxDefinitionInjectionIndex.typeIndexKey(type)].orEmpty()
                !target.isNullOrEmpty() -> fileData[ParadoxDefinitionInjectionIndex.targetIndexKey(target)].orEmpty()
                else -> fileData[ParadoxDefinitionInjectionIndex.AllIndexKey].orEmpty()
            }
            infos.process { info -> processInfo(mode, target, type, project, file, info, consumer) }
        }
    }

    private fun processInfo(
        mode: String?,
        target: String?,
        type: String?,
        project: Project,
        file: VirtualFile,
        info: ParadoxDefinitionInjectionIndexInfo,
        consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>
    ): Boolean {
        if (mode != null && !info.mode.equals(mode, true)) return true
        if (target != null && info.target != target) return true
        if (type != null && info.type != type) return true
        info.bind(file, project)
        if (info.element == null) return true
        return consumer.process(info)
    }
}
