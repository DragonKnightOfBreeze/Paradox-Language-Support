package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.lang.index.ParadoxDefinitionInjectionIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 定义注入的查询器。
 */
class ParadoxDefinitionInjectionSearcher : QueryExecutorBase<ParadoxDefinitionInjectionIndexInfo, ParadoxDefinitionInjectionSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionInjectionSearch.Parameters, consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (ChronicleThreadContext.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val keys = setOf(
            createActualKey(context),
            PlsIndexUtil.createLazyKey(),
        )
        return PlsIndexService.processAllFileData(ParadoxDefinitionInjectionIndex::class.java, keys, context.project, context.scope, context.gameType) p@{ file, fileData ->
            val actualKey = createActualKey(context)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun createActualKey(context: Context): String {
        val name = context.target
        val type = context.type
        return when {
            !name.isNullOrEmpty() && !type.isNullOrEmpty() -> PlsIndexUtil.createNameTypeKey(name, type)
            !name.isNullOrEmpty() -> PlsIndexUtil.createNameKey(name)
            !type.isNullOrEmpty() -> PlsIndexUtil.createTypeKey(type)
            else -> PlsIndexUtil.createAllKey()
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxDefinitionInjectionIndexInfo, consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>): Boolean {
        if (!matchesMode(context, info)) return true
        if (!matchesType(context, info)) return true
        if (!matchesTarget(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesMode(context: Context, info: ParadoxDefinitionInjectionIndexInfo): Boolean {
        if (context.mode == null) return true
        return context.mode.equals(info.mode, true)
    }

    private fun matchesType(context: Context, info: ParadoxDefinitionInjectionIndexInfo): Boolean {
        if (context.type == null) return true
        return context.type == info.type
    }

    private fun matchesTarget(context: Context, info: ParadoxDefinitionInjectionIndexInfo): Boolean {
        if (context.target == null) return true
        return context.target == info.target
    }

    private fun ParadoxDefinitionInjectionSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(mode, target, type, gameType, project, scope)
    }

    private data class Context(
        val mode: String?,
        val target: String?,
        val type: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
