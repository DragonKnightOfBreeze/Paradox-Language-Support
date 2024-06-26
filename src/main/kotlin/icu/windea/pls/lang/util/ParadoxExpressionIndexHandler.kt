package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.*

object ParadoxExpressionIndexHandler {
    val inferredScopeContextAwareDefinitionTypes = arrayOf("scripted_trigger", "scripted_effect")
    
    fun <ID : ParadoxExpressionIndexId<T>, T : ParadoxExpressionInfo> processQuery(
        id: ID,
        project: Project,
        gameType: ParadoxGameType,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, fileData: List<T>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if(SearchScope.isEmptyScope(scope)) return true
        
        return doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = ParadoxExpressionIndex.INSTANCE.getFileData(file, project, id)
            if(fileData.isEmpty()) return@p true
            processor(file, fileData)
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
    
    @Suppress("UNUSED_PARAMETER")
    fun postIndexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        
    }
}