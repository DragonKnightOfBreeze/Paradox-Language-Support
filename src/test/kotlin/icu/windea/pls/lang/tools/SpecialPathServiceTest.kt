package icu.windea.pls.lang.tools

import icu.windea.pls.model.ParadoxGameType
import org.junit.Test

class SpecialPathServiceTest {
    private val service = SpecialPathServiceImpl()
    private val gameTypes = ParadoxGameType.getAll()

    @Test
    fun getSteamPath() {
        val path = service.getSteamPath()
        println("Steam path: $path")
    }

    @Test
    fun getSteamGamePath() {
        for (gameType in gameTypes) {
            val path = service.getSteamGamePath(gameType.steamId, gameType.title)
            println("Steam game path [${gameType.id}]: $path")
        }
    }

    @Test
    fun getSteamGameWorkshopPath() {
        for (gameType in gameTypes) {
            val path = service.getSteamGameWorkshopPath(gameType.steamId)
            println("Steam workshop path [${gameType.id}]: $path")
        }
    }

    @Test
    fun getGameDataPath() {
        for (gameType in gameTypes) {
            val path = service.getGameDataPath(gameType)
            println("Game data path [${gameType.id}]: $path")
        }
    }
}
