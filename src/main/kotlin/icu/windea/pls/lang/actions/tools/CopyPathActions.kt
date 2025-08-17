package icu.windea.pls.lang.actions.tools

import icu.windea.pls.*
import icu.windea.pls.model.*
import java.nio.file.*

interface CopyPathActions {
    class Steam : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            return PlsFacade.getDataProvider().getSteamPath()
        }
    }

    class SteamGame : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
        }
    }

    class GameData : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        }
    }

    class Game : CopyPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxRootInfo.Game) return null
            return fileInfo.rootInfo.rootPath
        }
    }

    class Mod : CopyPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxRootInfo.Mod) return null
            return fileInfo.rootInfo.rootPath
        }
    }
}
