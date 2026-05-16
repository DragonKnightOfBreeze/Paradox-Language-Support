package icu.windea.pls.ep.tools

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.tools.SpecialPathService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

interface SpecialPathProviders {
    class Steam : SpecialPathProviderBase() {
        override val text get() = PlsBundle.message("special.path.steam")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            return SpecialPathService.getInstance().getSteamPath()
        }
    }

    class SteamGame : SpecialPathProviderBase() {
        override val text get() = PlsBundle.message("special.path.steamGame")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = selectGameType(file, gameType)
            return SpecialPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : SpecialPathProviderBase() {
        override val text get() = PlsBundle.message("special.path.steamWorkshop")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = selectGameType(file, gameType)
            return SpecialPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
        }
    }

    class GameData : SpecialPathProviderBase() {
        override val text get() = PlsBundle.message("special.path.gameData")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val gameType = selectGameType(file, gameType)
            return SpecialPathService.getInstance().getGameDataPath(gameType)
        }
    }

    class Game : SpecialPathProviderBase() {
        override val text get() = PlsBundle.message("special.path.game")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val rootInfo = selectRootInfo(file)
            if (rootInfo !is ParadoxRootInfo.Game) return null
            return rootInfo.metadata.rootPath
        }
    }

    class Mod : SpecialPathProviderBase() {
        override val text get() = PlsBundle.message("special.path.mod")

        override fun getPath(file: VirtualFile?, gameType: ParadoxGameType?): Path? {
            val rootInfo = selectRootInfo(file)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            return rootInfo.metadata.rootPath
        }
    }
}
