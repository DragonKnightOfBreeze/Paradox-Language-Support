package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.indexInfo.ParadoxDynamicValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 动态值的查询器。
 */
class ParadoxDynamicValueSearcher : QueryExecutorBase<ParadoxDynamicValueIndexInfo, ParadoxDynamicValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDynamicValueSearch.SearchParameters, consumer: Processor<in ParadoxDynamicValueIndexInfo>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return

        val name = queryParameters.name
        val dynamicValueTypes = queryParameters.dynamicValueTypes
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType ?: return

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val infos = ParadoxIndexInfoType.DynamicValue.findInfos(file, project)
            if (infos.isEmpty()) return@p true
            infos.forEach f@{ info ->
                if (info.dynamicValueType !in dynamicValueTypes) return@f
                if (name != null && name != info.name) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if (!r) return@p false
            }

            true
        }
    }

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope).let { if (!it) return false }
        FileTypeIndex.processFiles(ParadoxLocalisationFileType, processor, scope).let { if (!it) return false }
        return true
    }
}
