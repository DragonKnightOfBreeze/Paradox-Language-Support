package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.index.CwtConfigSymbolIndex
import icu.windea.pls.lang.search.CwtConfigSymbolSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.CwtConfigSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo

/**
 * 规则符号的查询器。
 */
class CwtConfigSymbolSearcher : QueryExecutorBase<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.Parameters>() {
    override fun processQuery(queryParameters: CwtConfigSymbolSearch.Parameters, consumer: Processor<in CwtConfigSymbolIndexInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(CwtFileType)
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in CwtConfigSymbolIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val keys = context.types
        return ChronicleIndexService.processAllFileData(CwtConfigSymbolIndex::class.java, keys, context.project, context.scope, context.gameType) { file, fileData ->
            context.types.process { type ->
                val infos = fileData[type].orEmpty()
                infos.process { info -> processInfo(context, file, info, consumer) }
            }
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: CwtConfigSymbolIndexInfo, consumer: Processor<in CwtConfigSymbolIndexInfo>): Boolean {
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesName(context: Context, info: CwtConfigSymbolIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name == info.name
    }

    private fun CwtConfigSymbolSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, types, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val types: Collection<String>,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : CwtConfigSearchContext
}
