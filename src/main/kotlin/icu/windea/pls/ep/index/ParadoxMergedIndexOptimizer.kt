package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.index.ParadoxMergedIndexType
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 用于优化构建合并索引时的性能。例如在文件级别过滤、在定义级别过滤，等等。
 *
 * @see ParadoxMergedIndex
 * @see ParadoxIndexInfo
 */
interface ParadoxMergedIndexOptimizer {
    /** 在脚本文件级别检查可用的合并索引类型。 */
    fun getAvailableTypes(file: ParadoxScriptFile): Collection<ParadoxMergedIndexType<*>> = emptySet()

    /** 在本地化文件级别检查可用的合并索引类型。 */
    fun getAvailableTypes(file: ParadoxLocalisationFile): Collection<ParadoxMergedIndexType<*>> = emptySet()

    /** 在 CSV 文件级别检查可用的合并索引类型。 */
    fun getAvailableTypes(file: ParadoxCsvFile): Collection<ParadoxMergedIndexType<*>> = emptySet()

    /** 在定义候选级别（包括定义和定义注入）检查可用的合并索引类型。 */
    fun getAvailableTypes(definitionCandidateInfo: ParadoxDefinitionCandidateInfo): Collection<ParadoxMergedIndexType<*>> = emptySet()

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxMergedIndexOptimizer>("icu.windea.pls.mergedIndexOptimizer")
    }
}
