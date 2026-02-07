package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.index.ParadoxDefineIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxDefineIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 预定义的命名空间与变量的查询器。
 */
class ParadoxDefineSearcher : QueryExecutorBase<ParadoxDefineIndexInfo, ParadoxDefineSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefineSearch.SearchParameters, consumer: Processor<in ParadoxDefineIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType).withFilePath("common/defines", "txt")
        if (SearchScope.isEmptyScope(scope)) return
        val gameType = queryParameters.gameType

        val variable = queryParameters.variable
        val namespace = queryParameters.namespace

        val keys = namespace.to.singletonSetOrEmpty()
        PlsIndexService.processAllFileData(ParadoxDefineIndex::class.java, keys, project, scope, gameType) p@{ file, fileData ->
            if (fileData.isEmpty()) return@p true
            if (namespace != null) {
                val map = fileData[namespace].orEmpty()
                if (variable != null) {
                    val info = map[variable]
                    processInfo(queryParameters, file, info, consumer)
                } else {
                    map.values.process { info -> processInfo(queryParameters, file, info, consumer) }
                }
            } else {
                fileData.values.process { map ->
                    if (variable != null) {
                        val info = map[variable]
                        processInfo(queryParameters, file, info, consumer)
                    } else {
                        map.values.process { info -> processInfo(queryParameters, file, info, consumer) }
                    }
                }
            }
        }
    }

    private fun processInfo(
        queryParameters: ParadoxDefineSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxDefineIndexInfo?,
        consumer: Processor<in ParadoxDefineIndexInfo>
    ): Boolean {
        if (info == null) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }
}
