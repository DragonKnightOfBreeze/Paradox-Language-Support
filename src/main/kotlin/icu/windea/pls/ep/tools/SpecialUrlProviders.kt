package icu.windea.pls.ep.tools

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.tools.SpecialUrlService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.ParadoxRootInfo

interface SpecialUrlProviders {
    class GameStore : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameStore")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameStoreUrl(gameType.steamId)
        }
    }

    class GameWorkshop : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameWorkshop")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameWorkshopUrl(gameType.steamId)
        }
    }

    class Mod : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.mod")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val rootInfo = ParadoxAnalysisManager.getSelectedRootInfo(file, gameType)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            val steamId = rootInfo.steamId ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrl(steamId)
        }
    }

    class GameStoreInSteam : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameStore.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameStoreUrlInSteam(gameType.steamId)
        }
    }

    class GameWorkshopInSteam : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameWorkshop.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameWorkshopUrlInSteam(gameType.steamId)
        }
    }

    class ModInSteam : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.mod.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val rootInfo = ParadoxAnalysisManager.getSelectedRootInfo(file, gameType)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            val steamId = rootInfo.steamId ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class GameLaunchInSteam : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameLaunch.inSteam")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamGameLaunchUrl(gameType.steamId)
        }
    }

    class GameInSteamDb : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameInSteamDb")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getSteamDbAppUrl(gameType.steamId)
        }
    }

    class GameInParadoxMods : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameInParadoxMods")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            val gameId = gameType.gameId.orNull() ?: return null
            return SpecialUrlService.getInstance().getParadoxModsGameUrl(gameId)
        }
    }

    class ModInParadoxMods : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.modInParadoxMods")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val rootInfo = ParadoxAnalysisManager.getSelectedRootInfo(file, gameType)
            if (rootInfo !is ParadoxRootInfo.Mod) return null
            if (rootInfo.source != ParadoxModSource.Paradox) return null
            val remoteId = rootInfo.remoteId ?: return null
            return SpecialUrlService.getInstance().getParadoxModsModUrl(remoteId)
        }
    }

    class GameForum : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameForum")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getGameForumUrl(gameType)
        }
    }

    class GameWiki : SpecialUrlProvider {
        override val text get() = ChronicleEpBundle.message("special.url.gameWiki")

        override fun getUrl(file: VirtualFile?, gameType: ParadoxGameType?): String? {
            val gameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)
            return SpecialUrlService.getInstance().getGameWikiUrl(gameType)
        }
    }
}
