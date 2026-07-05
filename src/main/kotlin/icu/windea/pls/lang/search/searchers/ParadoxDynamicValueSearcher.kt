package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 动态值的查询器。
 */
class ParadoxDynamicValueSearcher : QueryExecutorBase<ParadoxDynamicValueIndexInfo, ParadoxDynamicValueSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxDynamicValueSearch.Parameters, consumer: Processor<in ParadoxDynamicValueIndexInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType, ParadoxLocalisationFileType, ParadoxCsvFileType)
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxDynamicValueIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val indexInfoType = ParadoxIndexInfoTypes.DynamicValue
        return ChronicleIndexService.processAllFileDataWithKey(indexInfoType, context.project, context.scope, context.gameType) { file, infos ->
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxDynamicValueIndexInfo, consumer: Processor<in ParadoxDynamicValueIndexInfo>): Boolean {
        if (!matchesType(context, info)) return true
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesType(context: Context, info: ParadoxDynamicValueIndexInfo): Boolean {
        return context.types.contains(info.dynamicValueType)
    }

    private fun matchesName(context: Context, info: ParadoxDynamicValueIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name == info.name
    }

    private fun ParadoxDynamicValueSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, types, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val types: Set<String>,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
