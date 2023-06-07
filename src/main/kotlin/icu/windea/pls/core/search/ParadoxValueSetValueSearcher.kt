package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

/**
 * 值集值的查询器。
 */
class ParadoxValueSetValueSearcher : QueryExecutorBase<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxValueSetValueSearch.SearchParameters, consumer: Processor<in ParadoxValueSetValueInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        
        val name = queryParameters.name
        val valueSetNames = queryParameters.valueSetNames
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType ?: return
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            doProcessFiles(scope) p@{ file ->
                ProgressManager.checkCanceled()
                file.toPsiFile(project) ?: return@p true //NOTE 这里需要先获取psiFile，否则fileInfo可能未被解析
                if(selectGameType(file) != gameType) return@p true //check game type at file level
                
                val fileData = ParadoxValueSetValueFastIndex.getFileData(file, project)
                if(fileData.isEmpty()) return@p true
                valueSetNames.forEach f@{ valueSetName ->
                    val valueSetValueInfoList = fileData[valueSetName]
                    if(valueSetValueInfoList.isNullOrEmpty()) return@f
                    valueSetValueInfoList.forEachFast { info ->
                        if(name == null || name == info.name) {
                            val r = consumer.process(info)
                            if(!r) return@p false
                        }
                    }
                }
                
                true
            }
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope).let { if(!it) return false }
        FileTypeIndex.processFiles(ParadoxLocalisationFileType, processor, scope).let { if(!it) return false }
        return true
        
        ////use parallel processor to optimize performance
        //val parallelProcessor = ParallelProcessor(processor, ParadoxValueSetValueFastIndex.executorService)
        //FileTypeIndex.processFiles(ParadoxScriptFileType, parallelProcessor, scope)
        //FileTypeIndex.processFiles(ParadoxLocalisationFileType, parallelProcessor, scope)
        //return parallelProcessor.getResult()
    }
}

