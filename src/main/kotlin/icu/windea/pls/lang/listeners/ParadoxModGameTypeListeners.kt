package icu.windea.pls.lang.listeners

import icu.windea.pls.ide.analysis.ChronicleAnalysisManager
import icu.windea.pls.lang.settings.ChronicleProfilesSettings
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.model.ParadoxGameType

/**
 * 当更改模组的游戏类型后，重新解析文件。
 */
class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val gameType = modSettings.gameType

        modSettings.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) }
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) } }

        // 更新游戏类型信息缓存
        ChronicleProfilesSettings.getInstance().state.updateSettings()

        val modDirectories = mutableSetOf<String>()
        modSettings.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) }
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) } }

        // 刷新分析数据
        val rootFiles = ChronicleAnalysisManager.findRootFilesByRootFilePaths(modDirectories)
        ChronicleAnalysisManager.refreshAnalysisData(rootFiles)

        // 重新解析文件
        val files = ChronicleAnalysisManager.findAllFilesByRootFilePaths(modDirectories)
        ChronicleAnalysisManager.reparseFiles(files)
    }

    private fun refreshGameType(modDirectory: String, gameType: ParadoxGameType?) {
        val settings = ChronicleProfilesSettings.getInstance().state.modDescriptorSettings.get(modDirectory) ?: return
        settings.gameType = gameType
    }
}


