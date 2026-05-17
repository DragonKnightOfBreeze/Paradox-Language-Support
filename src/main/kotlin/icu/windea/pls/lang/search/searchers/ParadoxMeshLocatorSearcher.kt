package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.ParadoxMeshLocatorSearch
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo

/**
 * 网格定位器（mesh locator）的查询器。
 */
class ParadoxMeshLocatorSearcher : QueryExecutorBase<ParadoxMeshLocatorIndexInfo, ParadoxMeshLocatorSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxMeshLocatorSearch.Parameters, consumer: Processor<in ParadoxMeshLocatorIndexInfo>) {
        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxMeshLocatorIndexInfo>): Boolean {
        if (!context.isValid()) return true
        ProgressManager.checkCanceled()
        val indexInfoType = ParadoxIndexInfoTypes.MeshLocator
        return PlsIndexService.processAllFileDataWithKey(indexInfoType, context.project, context.scope, context.gameType) { file, infos ->
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxMeshLocatorIndexInfo, consumer: Processor<in ParadoxMeshLocatorIndexInfo>): Boolean {
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesName(context: Context, info: ParadoxMeshLocatorIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name == info.name
    }

    private fun ParadoxMeshLocatorSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
