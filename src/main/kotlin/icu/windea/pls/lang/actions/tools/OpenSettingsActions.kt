package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.project.Project
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.settings.ParadoxGameSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.ui.tools.ParadoxGameSettingsDialog
import icu.windea.pls.lang.ui.tools.ParadoxModSettingsDialog
import icu.windea.pls.model.ParadoxRootInfo

interface OpenSettingsActions {
    /**
     * 打开游戏配置。当前文件是项目中的游戏文件或目录时启用。
     *
     * @see ParadoxGameSettingsState
     * @see icu.windea.pls.lang.ui.tools.ParadoxGameSettingsDialog
     */
    class Game : OpenSettingsAction() {
        override fun isAvailable(rootInfo: ParadoxRootInfo): Boolean {
            return rootInfo is ParadoxRootInfo.Game
        }

        override fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project) {
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return
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
     * @see icu.windea.pls.lang.ui.tools.ParadoxModSettingsDialog
     */
    class Mod : OpenSettingsAction() {
        override fun isAvailable(rootInfo: ParadoxRootInfo): Boolean {
            return rootInfo is ParadoxRootInfo.Mod
        }

        override fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project) {
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return
            val rootPath = rootInfo.rootFile.path
            val modSettings = PlsFacade.getProfilesSettings().modSettings.get(rootPath) ?: return
            val dialog = ParadoxModSettingsDialog(project, modSettings)
            dialog.show()
        }
    }
}
