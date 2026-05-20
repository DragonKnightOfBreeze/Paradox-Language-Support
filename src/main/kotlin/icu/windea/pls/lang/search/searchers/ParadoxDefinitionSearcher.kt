package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.letIf
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxDefinitionIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.search.util.getConstraint
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxDefinitionIndexInfo, ParadoxDefinitionSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.Parameters, consumer: Processor<in ParadoxDefinitionIndexInfo>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxDefinitionIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val keys = setOf(
            createActualKey(context),
            PlsIndexUtil.createLazyKey(),
        )
        val r = PlsIndexService.processAllFileData(ParadoxDefinitionIndex::class.java, keys, context.project, context.scope, context.gameType) { file, fileData ->
            val actualKey = createActualKey(context)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
        if (!r) return false

        // process for swapped types
        val swappedTypeConfig = context.swappedTypeConfig
        if (swappedTypeConfig != null) {
            val r = processQuery(context.copy(type = swappedTypeConfig.name, subtypes = emptyList()), consumer)
            if (!r) return false
        }

        return true
    }

    private fun createActualKey(context: Context): String {
        val ignoreCase = context.constraint?.ignoreCase == true
        val name = context.name?.letIf(ignoreCase) { it.lowercase() }
        val type = context.type
        return when {
            !name.isNullOrEmpty() && !type.isNullOrEmpty() -> PlsIndexUtil.createNameTypeKey(name, type)
            !name.isNullOrEmpty() -> PlsIndexUtil.createNameKey(name)
            !type.isNullOrEmpty() -> PlsIndexUtil.createTypeKey(type)
            else -> PlsIndexUtil.createAllKey()
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxDefinitionIndexInfo, consumer: Processor<in ParadoxDefinitionIndexInfo>): Boolean {
        if (!matchesType(context, info)) return true
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        if (!matchesSubtypes(context, info)) return true
        return consumer.process(info)
    }

    private fun matchesName(context: Context, info: ParadoxDefinitionIndexInfo): Boolean {
        if (context.name == null) return true
        val ignoreCase = context.constraint?.ignoreCase == true
        return context.name.equals(info.name, ignoreCase)
    }

    private fun matchesType(context: Context, info: ParadoxDefinitionIndexInfo): Boolean {
        if (context.type == null) return true
        return context.type == info.type
    }

    private fun matchesSubtypes(context: Context, info: ParadoxDefinitionIndexInfo): Boolean {
        if (context.subtypes.isNullOrEmpty()) return true
        if (context.typeConfig?.subtypes.isNullOrEmpty()) return true // fast return

        // 检查是否匹配索引数据中的子类型
        val fastSubtypes = info.fastSubtypes
        if (fastSubtypes.isNotEmpty() && fastSubtypes.containsAll(context.subtypes)) return true

        // 检查索引中的子类型可能不包含继承的子类型，需要通过 PSI 获取完整子类型进行二次检查
        val element = info.element ?: return false
        val subtypes = ParadoxDefinitionManager.getSubtypes(element) ?: return false
        return subtypes.containsAll(context.subtypes)
    }

    private fun ParadoxDefinitionSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        val typeExpression = typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val type = typeExpression?.type
        val subtypes = typeExpression?.subtypes
        val constraint = selector.getConstraint() as? ParadoxDefinitionIndexConstraint
        return Context(name, type, subtypes, constraint, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val type: String?,
        val subtypes: List<String>?,
        val constraint: ParadoxDefinitionIndexConstraint?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext {
        val configGroup: CwtConfigGroup = PlsFacade.getConfigGroup(project, gameType)
        val typeConfig: CwtTypeConfig? = type?.orNull()?.let { configGroup.types[it] }
        val swappedType: String? = type?.orNull()?.let { configGroup.typesModel.base2Swapped[it] }
        val swappedTypeConfig: CwtTypeConfig? = swappedType?.orNull()?.let { configGroup.swappedTypes[it] }
    }
}
