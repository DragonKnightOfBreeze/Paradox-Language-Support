package icu.windea.pls.lang.listeners

import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType

/**
 * 当更改默认游戏类型后，重新解析文件。
 */
class ParadoxRefreshOnDefaultGameTypeChangedListener : ParadoxDefaultGameTypeListener {
    override fun onChange(oldGameType: ParadoxGameType, newGameType: ParadoxGameType) {
        val modDirectories = mutableSetOf<String>()
        PlsFacade.getProfilesSettings().modDescriptorSettings.values.forEach { settings ->
            if (settings.gameType == null) {
                //这里可能包含不在项目中（以及库中）的根目录
                settings.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) }
            }
        }

        //重新解析并刷新文件（IDE之后会自动请求重新索引）
        val files = PlsCoreManager.findFilesByRootFilePaths(modDirectories)
        PlsCoreManager.reparseFiles(files)
    }
}
