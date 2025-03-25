package icu.windea.pls.lang.listeners

import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*

/**
 * 当更改模组的游戏类型后，重新解析文件。
 */
class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val gameType = modSettings.gameType

        modSettings.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) }
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) } }

        //更新游戏类型信息缓存
        getProfilesSettings().updateSettings()

        val modDirectories = mutableSetOf<String>()
        modSettings.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) }
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) } }

        //重新解析文件（IDE之后会自动请求重新索引）
        val files = PlsManager.findFilesByRootFilePaths(modDirectories)
        PlsManager.reparseAndRefreshFiles(files)

        //此时不需要刷新内嵌提示
    }

    private fun refreshGameType(modDirectory: String, gameType: ParadoxGameType?) {
        val settings = getProfilesSettings().modDescriptorSettings.get(modDirectory) ?: return
        settings.gameType = gameType
    }
}


