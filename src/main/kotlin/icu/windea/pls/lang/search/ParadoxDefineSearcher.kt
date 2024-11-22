package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.*

/**
 * 预定义的命名空间与变量的查询器。
 */
class ParadoxDefineSearcher : QueryExecutorBase<ParadoxDefineInfo.Compact, ParadoxDefineSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefineSearch.SearchParameters, consumer: Processor<in ParadoxDefineInfo.Compact>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
            .withFilePath("common/scripted_variables", "txt")
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

            val fileData = ParadoxDefineInfoIndex.INSTANCE.getFileData(file, project)
            if (fileData.isEmpty()) return@p true
            if(namespace.isNotNullOrEmpty()) {
                val map = fileData[namespace]?: return@p true
                if(variable.isNotNullOrEmpty()) {
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
                    if(variable.isNotNullOrEmpty()) {
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
