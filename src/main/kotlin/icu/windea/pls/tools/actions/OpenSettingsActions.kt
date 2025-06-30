package icu.windea.pls.tools.actions

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.ui.*

interface OpenSettingsActions {
    /**
     * 打开游戏配置。当前文件是项目中的游戏文件或目录时启用。
     *
     * @see ParadoxGameSettingsState
     * @see ParadoxGameSettingsDialog
     */
    class Game : OpenSettingsAction() {
        override fun isAvailable(rootInfo: ParadoxRootInfo): Boolean {
            return rootInfo is ParadoxRootInfo.Game
        }

        override fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project) {
            val rootPath = rootInfo.rootFile.path
            val gameSettings = PlsFacade.getProfilesSettings().gameSettings.get(rootPath) ?: return
            val dialog = ParadoxGameSettingsDialog(project, gameSettings)
            dialog.show()
        }
    }

    /**
     * 打开模组配置。当前文件是项目中的模组文件或目录时启用。
     *
     * @see ParadoxModSettingsState
     * @see ParadoxModSettingsDialog
     */
    class Mod : OpenSettingsAction() {
        override fun isAvailable(rootInfo: ParadoxRootInfo): Boolean {
            return rootInfo is ParadoxRootInfo.Mod
        }

        override fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project) {
            val rootPath = rootInfo.rootFile.path
            val modSettings = PlsFacade.getProfilesSettings().modSettings.get(rootPath) ?: return
            val dialog = ParadoxModSettingsDialog(project, modSettings)
            dialog.show()
        }
    }
}
