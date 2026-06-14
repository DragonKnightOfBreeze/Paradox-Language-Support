package icu.windea.pls.lang.actions.settings

import com.intellij.openapi.project.Project
import icu.windea.pls.lang.settings.ParadoxGameSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.lang.ui.settings.ParadoxGameSettingsDialog
import icu.windea.pls.lang.ui.settings.ParadoxModSettingsDialog
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 打开游戏设置。当前文件是项目中的游戏文件或目录时启用。
 *
 * @see ParadoxGameSettingsState
 * @see ParadoxGameSettingsDialog
 */
class OpenGameSettingsAction : OpenSettingsActionBase() {
    override fun isAvailable(rootInfo: ParadoxRootInfo): Boolean {
        return rootInfo is ParadoxRootInfo.Game
    }

    override fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project) {
        if (rootInfo !is ParadoxRootInfo.Game) return
        val rootPath = rootInfo.rootFile.path
        val gameSettings = PlsProfilesSettings.getInstance().state.gameSettings.get(rootPath) ?: return
        val dialog = ParadoxGameSettingsDialog(project, rootInfo, gameSettings)
        dialog.show()
    }
}

/**
 * 打开模组设置。当前文件是项目中的模组文件或目录时启用。
 *
 * @see ParadoxModSettingsState
 * @see ParadoxModSettingsDialog
 */
class OpenModSettingsAction : OpenSettingsActionBase() {
    override fun isAvailable(rootInfo: ParadoxRootInfo): Boolean {
        return rootInfo is ParadoxRootInfo.Mod
    }

    override fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project) {
        if (rootInfo !is ParadoxRootInfo.Mod) return
        val rootPath = rootInfo.rootFile.path
        val modSettings = PlsProfilesSettings.getInstance().state.modSettings.get(rootPath) ?: return
        val dialog = ParadoxModSettingsDialog(project, rootInfo, modSettings)
        dialog.show()
    }
}
