package icu.windea.pls.ep.tools

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.tools.SpecialUrlService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo

interface SpecialUrlProviders {
    class GameStore : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.gameStore")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = selectGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameStoreUrl(gameType.steamId)
        }
    }

    class GameWorkshop : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.gameWorkshop")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = selectGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameWorkshopUrl(gameType.steamId)
        }
    }

    class Mod : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.mod")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val rootInfo = selectRootInfo(file)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            val steamId = rootInfo.steamId ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrl(steamId)
        }
    }

    class GameStoreInSteam : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.gameStore.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = selectGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameStoreUrlInSteam(gameType.steamId)
        }
    }

    class GameWorkshopInSteam : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.gameWorkshop.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = selectGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameWorkshopUrlInSteam(gameType.steamId)
        }
    }

    class ModInSteam : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.mod.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val rootInfo = selectRootInfo(file)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            val steamId = rootInfo.steamId ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class GameLaunchInSteam : SpecialUrlProviderBase() {
        override val text get() = PlsBundle.message("special.url.gameLaunch.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = selectGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameLaunchUrl(gameType.steamId)
        }
    }
}
