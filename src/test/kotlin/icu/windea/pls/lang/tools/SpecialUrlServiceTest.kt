package icu.windea.pls.lang.tools

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

class SpecialUrlServiceTest {
    private val service = SpecialUrlServiceImpl()
    private val gameTypes = ParadoxGameType.getAll()
    private val map = mutableMapOf(
        "UI Overhaul Dynamic" to "1623423360"
    )

    @Test
    fun getSteamGameStoreUrl() {
        for (gameType in gameTypes) {
            val url = service.getSteamGameStoreUrl(gameType.steamId)
            println("Steam game store url [${gameType.id}]: $url")
        }
    }

    @Test
    fun getSteamGameWorkshopUrl() {
        for (gameType in gameTypes) {
            val url = service.getSteamGameWorkshopUrl(gameType.steamId)
            println("Steam game workshop url [${gameType.id}]: $url")
        }
    }

    @Test
    fun getSteamWorkshopUrl() {
        for ((name, steamId) in map) {
            val url = service.getSteamWorkshopUrl(steamId)
            println("Steam workshop url [$name]: $url")
        }
    }

    @Test
    fun getSteamGameStoreUrlInSteam() {
        for (gameType in gameTypes) {
            val url = service.getSteamGameStoreUrlInSteam(gameType.steamId)
            println("Steam game store url [${gameType.id}]: $url")
        }
    }

    @Test
    fun getSteamGameWorkshopUrlInSteam() {
        for (gameType in gameTypes) {
            val url = service.getSteamGameWorkshopUrlInSteam(gameType.steamId)
            println("Steam game workshop url [${gameType.id}]: $url")
        }
    }

    @Test
    fun getSteamWorkshopUrlInSteam() {
        for ((name, steamId) in map) {
            val url = service.getSteamWorkshopUrlInSteam(steamId)
            println("Steam workshop url [$name]: $url")
        }
    }

    @Test
    fun getSteamGameLaunchUrl() {
        for (gameType in gameTypes) {
            val url = service.getSteamGameLaunchUrl(gameType.steamId)
            println("Steam game launch url [${gameType.id}]: $url")
        }
    }

    @Test
    fun getGameWikiUrl() {
        for (gameType in gameTypes) {
            val url = service.getGameWikiUrl(gameType)
            println("Game wiki url [${gameType.id}]: $url")
        }
    }
}
