package icu.windea.pls.lang

import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.core.util.FilePathBasedModificationTracker
import java.util.concurrent.ConcurrentHashMap

/**
 * 用于追踪更改 - 具有更高的精确度，提高缓存命中率。
 */
object ParadoxModificationTrackers {
    /** 追踪任意游戏或模组目录中的脚本文件、本地化文件或 CSV 文件的更改。 */
    val FileTracker = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的脚本文件的更改。 */
    val ScriptFileTracker = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的本地化文件的更改。 */
    val LocalisationFileTracker = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的 CSV 文件的更改。 */
    val CsvFileTracker = SimpleModificationTracker()

    val ScriptFileTrackers = ConcurrentHashMap<String, FilePathBasedModificationTracker>()

    fun ScriptFileTracker(key: String): FilePathBasedModificationTracker {
        return ScriptFileTrackers.getOrPut(key) { FilePathBasedModificationTracker(key) }
    }

    val ScriptedVariablesTracker = ScriptFileTracker("common/scripted_variables/**/*.txt")
    val InlineScriptsTracker = ScriptFileTracker("common/inline_scripts/**/*.txt")

    val LocaleTracker = SimpleModificationTracker()

    val ParameterConfigInferenceTracker = SimpleModificationTracker()
    val InlineScriptConfigInferenceTracker = SimpleModificationTracker()
    val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()

    fun refreshAllFileTrackers() {
        FileTracker.incModificationCount()
        ScriptFileTracker.incModificationCount()
        LocalisationFileTracker.incModificationCount()
        CsvFileTracker.incModificationCount()
        ScriptFileTrackers.values.forEach { it.incModificationCount() }
    }
}
