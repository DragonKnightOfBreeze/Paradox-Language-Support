package icu.windea.pls.lang.search

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.processAllElements
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.model.indexInfo.ParadoxParameterIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

class ParadoxParameterSearcher : QueryExecutorBase<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxParameterSearch.SearchParameters, consumer: Processor<in ParadoxParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val contextKey = queryParameters.contextKey
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType

        // 2.0.6 优化：对于内联脚本的传入参数，可以改为通过存根索引查询
        run {
            val inlineScriptExpression = contextKey.removePrefixOrNull("inline_script@") ?: return@run
            processQueryForInlineScriptArguments(name, inlineScriptExpression, project, scope) p@{ p ->
                val stub = runReadAction { p.greenStub?.castOrNull<ParadoxScriptPropertyStub.InlineScriptArgument>() } ?: return@p true
                val info = ParadoxParameterIndexInfo(stub.argumentName, contextKey, ReadWriteAccessDetector.Access.Write, p.textOffset, stub.gameType)
                val file = p.containingFile.virtualFile ?: return@p true
                info.virtualFile = file
                consumer.process(info)
            }
            return
        }

        processFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) // ensure file info is resolved here
            if (gameType != null && selectGameType(file) != gameType) return@p true // check game type at file level

            val infos = ParadoxIndexInfoType.Parameter.findInfos(file, project)
            if (infos.isEmpty()) return@p true
            infos.forEach f@{ info ->
                if (contextKey != info.contextKey) return@f
                if (name != null && name != info.name) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if (!r) return@p false
            }

            true
        }
    }

    private fun processFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }

    private fun processQueryForInlineScriptArguments(
        name: String?,
        inlineScriptExpression: String,
        project: Project,
        scope: GlobalSearchScope,
        processor: Processor<ParadoxScriptProperty>
    ): Boolean {
        val indexKey = ParadoxIndexKeys.InlineScriptArgument
        return indexKey.processAllElements(inlineScriptExpression, project, scope) p@{ element ->
            if (name != null && name != element.name) return@p true
            processor.process(element)
        }
    }
}
