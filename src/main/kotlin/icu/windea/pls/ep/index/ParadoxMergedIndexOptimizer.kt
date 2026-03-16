package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 用于优化构建合并索引时的性能。例如在文件级别过滤、在表达式级别过滤，等等。
 */
interface ParadoxMergedIndexOptimizer {
    /** 检查在文件级别是否可用。任意优化器返回 `true` 即表示可用。 */
    fun isAvailableForFile(file: ParadoxScriptFile): Boolean = false

    /** 检查在文件级别是否可用。任意优化器返回 `true` 即表示可用。 */
    fun isAvailableForFile(file: ParadoxLocalisationFile): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxMergedIndexOptimizer>("icu.windea.pls.mergedIndexOptimizer")
    }
}
