package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.findFileBasedIndex
import icu.windea.pls.lang.index.ParadoxInlineScriptUsageIndex
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.model.indexInfo.ParadoxInlineScriptUsageIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 内联脚本使用的查询器。
 */
class ParadoxInlineScriptUsageSearcher : QueryExecutorBase<ParadoxInlineScriptUsageIndexInfo.Compact, ParadoxInlineScriptUsageSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptUsageSearch.SearchParameters, consumer: Processor<in ParadoxInlineScriptUsageIndexInfo.Compact>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val expression = queryParameters.expression
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType

        if (expression.isNotEmpty() && expression.isParameterized()) return // skip if expression is parameterized

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val fileData = findFileBasedIndex<ParadoxInlineScriptUsageIndex>().getFileData(file, project)
            if (fileData.isEmpty()) return@p true
            if (expression.isNotEmpty()) {
                val info = fileData[expression] ?: return@p true
                info.virtualFile = file
                val r = consumer.process(info)
                if (!r) return@p false
            } else {
                fileData.values.forEach { info ->
                    info.virtualFile = file
                    val r = consumer.process(info)
                    if (!r) return@p false
                }
            }

            true
        }
    }

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}
