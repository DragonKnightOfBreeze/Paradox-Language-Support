package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
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
        val scope = queryParameters.selector.scope.withFileTypes(ParadoxScriptFileType).withFilePath("common/defines", "txt")
        if (SearchScope.isEmptyScope(scope)) return
        val gameType = queryParameters.selector.gameType ?: return

        val variable = queryParameters.variable
        val namespace = queryParameters.namespace

        val keys = namespace.singleton.setOrEmpty()
        PlsIndexService.processAllFileData(ParadoxDefineIndex::class.java, keys, project, gameType, scope) p@{ file, fileData ->
            if (namespace != null) {
                val map = fileData[namespace] ?: return@p true
                if (variable != null) {
                    val info = map[variable] ?: return@p true
                    info.virtualFile = file
                    consumer.process(info)
                } else {
                    map.values.process { info ->
                        info.virtualFile = file
                        consumer.process(info)
                    }
                }
            } else {
                fileData.values.process { map ->
                    if (variable != null) {
                        val info = map[variable] ?: return@p true
                        info.virtualFile = file
                        consumer.process(info)
                    } else {
                        map.values.process { info ->
                            info.virtualFile = file
                            consumer.process(info)
                        }
                    }
                }
            }
        }
    }
}
