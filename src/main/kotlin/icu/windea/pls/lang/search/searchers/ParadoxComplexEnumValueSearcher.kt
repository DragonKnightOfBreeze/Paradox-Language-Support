package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.ParadoxComplexEnumValueIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 复杂枚举值的查询器。
 */
class ParadoxComplexEnumValueSearcher : QueryExecutorBase<ParadoxComplexEnumValueIndexInfo, ParadoxComplexEnumValueSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxComplexEnumValueSearch.Parameters, consumer: Processor<in ParadoxComplexEnumValueIndexInfo>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (ChronicleThreadContext.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxComplexEnumValueIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val keys = setOf(
            createActualKey(context),
            PlsIndexUtil.createLazyKey(),
        )
        return PlsIndexService.processAllFileData(ParadoxComplexEnumValueIndex::class.java, keys, context.project, context.scope, context.gameType) { file, fileData ->
            val actualKey = createActualKey(context)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun createActualKey(context: Context): String {
        val type = context.enumName
        return PlsIndexUtil.createTypeKey(type)
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxComplexEnumValueIndexInfo, consumer: Processor<in ParadoxComplexEnumValueIndexInfo>): Boolean {
        if (!matchesEnumName(context, info)) return true
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesEnumName(context: Context, info: ParadoxComplexEnumValueIndexInfo): Boolean {
        return context.enumName == info.enumName
    }

    private fun matchesName(context: Context, info: ParadoxComplexEnumValueIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name.equals(info.name, info.caseInsensitive) // # 261
    }


    private fun ParadoxComplexEnumValueSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, enumName, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val enumName: String,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
