package icu.windea.pls.lang.search.scope

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtConfigMatchService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.script.ParadoxScriptFileType

@Suppress("EqualsOrHashCode")
class ParadoxWithConfigSearchScope(
    val delegate: GlobalSearchScope,
    val configName: String,
    val configType: CwtConfigType,
    val configGroup: CwtConfigGroup,
) : ParadoxSearchScope(delegate.project, null) {
    val config by lazy { CwtConfigMatchService.getConfigToMatchFilePath(configName, configType, configGroup) }

    override fun getDisplayName(): String {
        return ChronicleBundle.message("search.scope.name.withConfig", delegate.displayName, configName, configType.id, configGroup.gameType)
    }

    override fun contains(file: VirtualFile): Boolean {
        if (!delegate.contains(file)) return false // NOTE 3.0.1 should check delegate first
        if (file.fileType !== ParadoxScriptFileType) return true // skip if current file is not a script file (can be, e.g., a csv file instead)
        if (config == null) return true // Skip if there is no corresponding config
        return super.contains(file)
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        if (topFile.fileType !== ParadoxScriptFileType) return true // skip if current file is not a script file (can be, e.g., a csv file instead)
        val config = config ?: return true // Skip if there is no corresponding config
        val fileInfo = topFile.fileInfo ?: return false
        val path = fileInfo.path
        return CwtConfigMatchService.matchesFilePath(config, path) // NOTE 3.0.1 restrict file by check whether corresponding config can be matched by the file path
    }

    override fun calcHashCode(): Int {
        var result = delegate.hashCode()
        result = result * 31 + configName.hashCode()
        result = result * 31 + configType.hashCode()
        result = result * 31 + configGroup.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxWithConfigSearchScope
            && delegate == other.delegate
            && configName == other.configName
            && configType == other.configType
            && configGroup == other.configGroup
    }

    override fun toString(): String {
        return "$delegate - with config of name ${configName} and type ${configType} in config group ${configGroup}"
    }
}
