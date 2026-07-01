package icu.windea.pls.lang.listeners

import icu.windea.pls.ide.analysis.ChronicleAnalysisManager
import icu.windea.pls.lang.settings.ChronicleProfilesSettings
import icu.windea.pls.model.ParadoxGameType

/**
 * 当更改默认游戏类型后，重新解析文件。
 */
class ParadoxRefreshOnDefaultGameTypeChangedListener : ParadoxDefaultGameTypeListener {
    override fun onChange(oldGameType: ParadoxGameType, newGameType: ParadoxGameType) {
        val modDirectories = mutableSetOf<String>()
        ChronicleProfilesSettings.getInstance().state.modDescriptorSettings.values.forEach { settings ->
            if (settings.gameType == null) {
                // 这里可能包含不在项目中（以及库中）的根目录
                settings.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) }
            }
        }

        // 刷新分析数据
        val rootFiles = ChronicleAnalysisManager.findRootFilesByRootFilePaths(modDirectories)
        ChronicleAnalysisManager.refreshAnalysisData(rootFiles)

        // 重新解析文件
        val files = ChronicleAnalysisManager.findAllFilesByRootFilePaths(modDirectories)
        ChronicleAnalysisManager.reparseFiles(files)
    }
}
