package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.*

/**
 * 预定义的命名空间与变量的查询器。
 */
class ParadoxDefineSearcher : QueryExecutorBase<ParadoxDefineIndexInfo.Compact, ParadoxDefineSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefineSearch.SearchParameters, consumer: Processor<in ParadoxDefineIndexInfo.Compact>) {
        ProgressManager.checkCanceled()
        if(queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
            .withFilePath("common/defines", "txt")
        if (SearchScope.isEmptyScope(scope)) return
        val namespace = queryParameters.namespace
        val variable = queryParameters.variable
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val fileData = ParadoxDefineIndex.INSTANCE.getFileData(file, project)
            if (fileData.isEmpty()) return@p true
            if(namespace != null) {
                val map = fileData[namespace]?: return@p true
                if(variable != null) {
                    val info = map[variable] ?: return@p true
                    info.virtualFile = file
                    val r = consumer.process(info)
                    if (!r) return@p false
                } else {
                    map.values.forEach { info ->
                        info.virtualFile = file
                        val r = consumer.process(info)
                        if (!r) return@p false
                    }
                }
            } else {
                fileData.values.forEach { map ->
                    if(variable != null) {
                        val info = map[variable] ?: return@p true
                        info.virtualFile = file
                        val r = consumer.process(info)
                        if (!r) return@p false
                    } else {
                        map.values.forEach { info ->
                            info.virtualFile = file
                            val r = consumer.process(info)
                            if (!r) return@p false
                        }
                    }
                }
            }

            true
        }
    }

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}
