package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.index.ChronicleIndexUtil
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.scope.withConfig
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.search.util.getConstraint
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 定义的查询器。
 *
 * @see ParadoxDefinitionSearch
 */
@Optimized
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxDefinitionIndexInfo, ParadoxDefinitionSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.Parameters, consumer: Processor<in ParadoxDefinitionIndexInfo>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (ChronicleThreadContext.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxDefinitionIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val constraint = context.constraint
        val indexId = constraint?.indexId ?: ChronicleIndexKeys.Definition
        val keys = setOf(
            createActualKey(context),
            ChronicleIndexUtil.createLazyKey(),
        )
        val r = ChronicleIndexService.processAllFileData(indexId, keys, context.project, context.scope, context.gameType) { file, fileData ->
            val actualKey = createActualKey(context)
            val infos = fileData[actualKey].orEmpty()
            infos.processFast { info -> processInfo(context, file, info, consumer) }
        }
        if (!r) return false

        // process for swapped types
        if (context.swappedType != null) {
            val r = processQuery(context.copy(type = context.swappedType, subtypes = emptyList()), consumer)
            if (!r) return false
        }

        return true
    }

    private fun createActualKey(context: Context): String {
        val constraint = context.constraint
        val ignoreCase = constraint?.ignoreCase == true
        val name = if (ignoreCase) context.name?.lowercase() else context.name
        val type = context.type
        return when {
            !name.isNullOrEmpty() && !type.isNullOrEmpty() -> ChronicleIndexUtil.createNameTypeKey(name, type)
            !name.isNullOrEmpty() -> ChronicleIndexUtil.createNameKey(name)
            !type.isNullOrEmpty() -> ChronicleIndexUtil.createTypeKey(type)
            else -> ChronicleIndexUtil.createAllKey()
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

    private fun ParadoxDefinitionSearch.Parameters.createContext(): Context {
        val typeExpression = typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val type = typeExpression?.type
        val subtypes = typeExpression?.subtypes
        val constraint = selector.getConstraint() as? ParadoxDefinitionIndexConstraint // extract index constraint from the selector
        val scope = scope.withFileTypes(ParadoxScriptFileType) // optimize: restrict file types
            .withConfig(type, CwtConfigTypes.Type, selector) // 3.0.1 optimize: restrict file by complex enum config
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
        val configGroup: CwtConfigGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val typeConfig: CwtTypeConfig? = type?.orNull()?.let { configGroup.types[it] }
        val swappedType: String? = type?.orNull()?.let { configGroup.typesModel.base2Swapped[it] }?.takeIf { it != type }
    }
}
