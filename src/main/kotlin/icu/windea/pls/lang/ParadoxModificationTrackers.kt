package icu.windea.pls.lang

import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.core.util.FilePathBasedModificationTracker
import icu.windea.pls.core.util.MergedModificationTracker
import java.util.concurrent.ConcurrentHashMap

/**
 * 用于追踪更改 - 具有更高的精确度，提高缓存命中率。
 */
object ParadoxModificationTrackers {
    /** 追踪任意游戏或模组目录中的脚本文件的更改。 */
    val ScriptFile = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的本地化文件的更改。 */
    val LocalisationFile = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的 CSV 文件的更改。 */
    val CsvFile = SimpleModificationTracker()

    val ScriptFileMap = ConcurrentHashMap<String, FilePathBasedModificationTracker>()

    fun ScriptFile(key: String): FilePathBasedModificationTracker {
        return ScriptFileMap.getOrPut(key) { FilePathBasedModificationTracker(key) }
    }

    val ScriptedVariables = ScriptFile("common/scripted_variables/**/*.txt")
    val InlineScripts = ScriptFile("common/inline_scripts/**/*.txt")

    val ParameterConfigInference = SimpleModificationTracker()
    val InlineScriptConfigInference = SimpleModificationTracker()
    val DefinitionScopeContextInference = SimpleModificationTracker()

    val PreferredLocale = SimpleModificationTracker()
    val FilePath = SimpleModificationTracker()
    val Match = MergedModificationTracker(
        ScriptFile,
        LocalisationFile,
        FilePath,
        ParameterConfigInference,
        InlineScriptConfigInference,
    )
    val Resolve = MergedModificationTracker(
        ScriptFile,
        LocalisationFile,
        FilePath,
    )
    val Scope = DefinitionScopeContextInference
}
