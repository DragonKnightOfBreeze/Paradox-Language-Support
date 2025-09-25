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
import icu.windea.pls.model.indexInfo.ParadoxParameterIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

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

        // // 优先：针对 inline_script 上下文，改为通过 StubIndex 查询“传参名”
        // if (contextKey.startsWith("inline_script@")) {
        //     val expr = contextKey.substringAfter('@')
        //     if (name != null) {
        //         val props = StubIndex.getElements(ParadoxIndexKeys.InlineScriptArgument, name, project, scope, ParadoxScriptProperty::class.java)
        //         publishInlineArgs(props, name, contextKey, gameType, consumer)
        //     } else {
        //         val keys = StubIndex.getInstance().getAllKeys(ParadoxIndexKeys.InlineScriptArgument, project)
        //         for (key in keys) {
        //             val props = StubIndex.getElements(ParadoxIndexKeys.InlineScriptArgument, key, project, scope, ParadoxScriptProperty::class.java)
        //             publishInlineArgs(props, key, contextKey, gameType, consumer)
        //         }
        //     }
        //     return
        // }

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
            if (gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level

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

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }

    // private fun publishInlineArgs(props: Collection<ParadoxScriptProperty>, name: String, contextKey: String, gameType: icu.windea.pls.model.ParadoxGameType?, consumer: Processor<in ParadoxParameterIndexInfo>) {
    //     if (props.isEmpty()) return
    //     for (p in props) {
    //         val vFile = p.containingFile.virtualFile ?: continue
    //         val gt = selectGameType(vFile)
    //         if (gameType != null && gt != gameType) continue
    //         val stub = p.greenStub as? ParadoxScriptPropertyStub.InlineScriptArgument ?: continue
    //         // 仅保留与 contextKey 对应的 inline_script 表达式
    //         val expr = contextKey.substringAfter('@')
    //         if (stub.inlineScriptExpression != expr) continue
    //         val info = ParadoxParameterIndexInfo(name, contextKey, ReadWriteAccessDetector.Access.Write, p.textOffset, gt ?: continue)
    //         info.virtualFile = vFile
    //         val r = consumer.process(info)
    //         if (!r) return
    //     }
    // }
}
