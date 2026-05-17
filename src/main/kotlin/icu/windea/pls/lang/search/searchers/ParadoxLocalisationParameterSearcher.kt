package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.ParadoxLocalisationParameterSearch
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo

/**
 * 本地化参数的查询器。
 */
class ParadoxLocalisationParameterSearcher : QueryExecutorBase<ParadoxLocalisationParameterIndexInfo, ParadoxLocalisationParameterSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationParameterSearch.Parameters, consumer: Processor<in ParadoxLocalisationParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxLocalisationParameterIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val indexInfoType = ParadoxIndexInfoTypes.LocalisationParameter
        return PlsIndexService.processAllFileDataWithKey(indexInfoType, context.project, context.scope, context.gameType) { file, infos ->
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxLocalisationParameterIndexInfo, consumer: Processor<in ParadoxLocalisationParameterIndexInfo>): Boolean {
        if (!matchesLocalisationName(context, info)) return true
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesLocalisationName(context: Context, info: ParadoxLocalisationParameterIndexInfo): Boolean {
        return context.localisationName == info.localisationName
    }

    private fun matchesName(context: Context, info: ParadoxLocalisationParameterIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name == info.name
    }

    private fun ParadoxLocalisationParameterSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, localisationName, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val localisationName: String,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
