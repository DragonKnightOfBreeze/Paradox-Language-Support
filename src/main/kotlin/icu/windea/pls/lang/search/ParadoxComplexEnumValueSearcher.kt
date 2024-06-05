package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.*

/**
 * 复杂枚举值的查询器。
 */
class ParadoxComplexEnumValueSearcher : QueryExecutorBase<ParadoxComplexEnumValueInfo, ParadoxComplexEnumValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, consumer: Processor<in ParadoxComplexEnumValueInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val enumName = queryParameters.enumName
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType ?: return
        
        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = ParadoxExpressionIndex.INSTANCE.getFileData(file, project, ParadoxExpressionIndexId.ComplexEnumValue)
            if(fileData.isEmpty()) return@p true
            fileData.forEachFast f@{ info ->
                if(enumName != info.enumName) return@f
                if(name != null && name != info.name) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if(!r) return@p false
            }
            
            true
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}

