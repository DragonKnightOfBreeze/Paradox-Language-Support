package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.model.*
import java.nio.file.*

interface GoToPathActions {
    class Steam : GoToPathAction() {
        override var targetPath: Path? = null

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (targetPath == null) {
                targetPath = PlsFacade.getDataProvider().getSteamPath()
            }
            return true
        }
    }

    class SteamGame : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)
            }
            return true
        }
    }

    class SteamWorkshop : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null
        override val expand: Boolean = true

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
            }
            return true
        }
    }

    class GameData : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null
        override val expand: Boolean = true

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
            }
            return true
        }
    }
}
