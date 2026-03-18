package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.index.ParadoxMergedIndex
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
    /** 检查在脚本文件级别是否可用。任意优化器返回 `true` 即表示可用。 */
    fun isAvailableForScriptFile(file: ParadoxScriptFile): Boolean = false

    /** 检查在本地化文件级别是否可用。任意优化器返回 `true` 即表示可用。 */
    fun isAvailableForLocalisationFile(file: ParadoxLocalisationFile): Boolean = false

    /** 检查在定义级别是否可用。任意优化器返回 `true` 即表示可用。 */
    fun isAvailableForDefinition(definitionCandidateInfo: ParadoxDefinitionCandidateInfo): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxMergedIndexOptimizer>("icu.windea.pls.mergedIndexOptimizer")
    }
}
