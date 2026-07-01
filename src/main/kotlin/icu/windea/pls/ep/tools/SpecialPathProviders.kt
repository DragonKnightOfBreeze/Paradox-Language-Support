package icu.windea.pls.ep.tools

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.lang.tools.SpecialPathService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

interface SpecialPathProviders {
    class Steam : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.steam")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            return SpecialPathService.getInstance().getSteamPath()
        }
    }

    class SteamGame : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.steamGame")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.steamWorkshop")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
        }
    }

    class GameData : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.gameData")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialPathService.getInstance().getGameDataPath(gameType)
        }
    }

    class Game : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.game")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val rootInfo = ParadoxAnalysisManager.getSelectedRootInfo(file, gameType)
            if (rootInfo !is ParadoxRootInfo.Game) return null
            return rootInfo.metadata.rootPath
        }
    }

    class Mod : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.mod")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val rootInfo = ParadoxAnalysisManager.getSelectedRootInfo(file, gameType)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            return rootInfo.metadata.rootPath
        }
    }

    class GameExecutable : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.gameExecutable")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val rootInfo = ParadoxAnalysisManager.getSelectedRootInfo(file, gameType)
            val gameType = rootInfo?.gameType ?: ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            val rootPath = (rootInfo as? ParadoxRootInfo.Game)?.metadata?.rootPath
                ?: SpecialPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
                ?: return null
            return ParadoxGameManager.getExecutablePath(gameType, rootPath)
        }
    }

    class GameModDirectory : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.gameModDirectory")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            val gameDataPath = SpecialPathService.getInstance().getGameDataPath(gameType) ?: return null
            return gameDataPath.resolve("mod")
        }
    }

    class GameSaveGames : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.gameSaveGames")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            val gameDataPath = SpecialPathService.getInstance().getGameDataPath(gameType) ?: return null
            return gameDataPath.resolve("save games")
        }
    }

    class GameLogs : SpecialPathProvider {
        override val text get() = ChronicleEpBundle.message("special.path.gameLogs")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            val gameDataPath = SpecialPathService.getInstance().getGameDataPath(gameType) ?: return null
            return gameDataPath.resolve("logs")
        }
    }
}
