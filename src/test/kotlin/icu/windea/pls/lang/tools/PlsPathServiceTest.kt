package icu.windea.pls.lang.tools

import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test
import icu.windea.pls.model.ParadoxGameType

class PlsPathServiceTest {
    private val service = PlsPathServiceImpl()

    @Before
    fun doSetUp() = AssumePredicates.includeLocalEnv()

    @Test
    fun getSteamPath() {
        val path = service.getSteamPath()
        println("Steam path: $path")
    }

    @Test
    fun getSteamGamePath() {
        for (gameType in ParadoxGameType.getAll()) {
            val path = service.getSteamGamePath(gameType.steamId, gameType.title)
            println("Steam game path [${gameType.id}]: $path")
        }
    }

    @Test
    fun getSteamGameWorkshopPath() {
        for (gameType in ParadoxGameType.getAll()) {
            val path = service.getSteamGameWorkshopPath(gameType.steamId)
            println("Steam workshop path [${gameType.id}]: $path")
        }
    }

    @Test
    fun getGameDataPath() {
        for (gameType in ParadoxGameType.getAll()) {
            val path = service.getGameDataPath(gameType)
            println("Game data path [${gameType.id}]: $path")
        }
    }
}
