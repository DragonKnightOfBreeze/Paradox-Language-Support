package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path

interface GoToPathActions {
    class Steam : GoToPathAction() {
        override var targetPath: Path? = null

        override fun isVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (targetPath == null) {
                targetPath = PlsPathService.getInstance().getSteamPath()
            }
            return true
        }
    }

    class SteamGame : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null

        override fun isVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
            }
            return true
        }
    }

    class SteamWorkshop : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null
        override val expand: Boolean = true

        override fun isVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsPathService.getInstance().getSteamWorkshopPath(gameType.steamId)
            }
            return true
        }
    }

    class GameData : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null
        override val expand: Boolean = true

        override fun isVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsPathService.getInstance().getGameDataPath(gameType.title)
            }
            return true
        }
    }
}
