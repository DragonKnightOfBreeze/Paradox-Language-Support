package icu.windea.pls.extensions.json

import com.intellij.json.JsonFileType
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxRootInfo

object JsonExtensionManager {
    // region launcher-settings.json

    fun isLauncherSettingsJson(file: VirtualFile): Boolean {
        // 2.1.8 relax check
        if (file.fileType !is JsonFileType) return false
        if (file.name != "launcher-settings.json") return false
        val fileInfo = file.fileInfo ?: return false
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return false
        return true // relax check
    }

    fun getLauncherSettingsJsonSchemaPath(): String {
        return "/jsonSchema/launcher-settings.schema.json"
    }

    // endregion

    // region metadata.json

    fun isMetadataJson(file: VirtualFile): Boolean {
        // 2.1.8 relax check
        if (file.fileType !is JsonFileType) return false
        if (file.name != "metadata.json") return false
        val fileInfo = file.fileInfo ?: return false
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return false
        return true
    }

    fun getMetadataJsonSchemaPath(): String {
        return "/jsonSchema/metadata.schema.json"
    }

    // endregion
}
