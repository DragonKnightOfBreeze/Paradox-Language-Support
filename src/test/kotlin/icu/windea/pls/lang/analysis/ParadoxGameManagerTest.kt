package icu.windea.pls.lang.analysis

import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class ParadoxGameManagerTest {
    @Test
    fun compareGameVersion() {
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.3") == 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.4") < 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.2") > 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.3.") == 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.3.0") < 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.3.1") < 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3.0", "3.3.1") < 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3.0", "3.3.*") == 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.3.*") == 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.4.*") < 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.3", "3.2.*") > 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.14", "3.6") > 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.14", "3.6.1") > 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.99.1 beta", "3.99.1 beta") == 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.99.1 beta", "3.99.1  beta") == 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.99.1", "3.99.1 beta") > 0)
        Assert.assertTrue(ParadoxGameManager.compareGameVersion("3.99.8 beta", "3.99.1 beta") > 0)
    }

    @RunWith(Parameterized::class)
    class PerGameType(private val gameType: ParadoxGameType) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = ParadoxGameType.getAll()
        }

        @Test
        fun getExecutablePath() {
            val service = PlsPathService.getInstance()

            val rootPath = service.getSteamGamePath(gameType.steamId, gameType.title)
            Assume.assumeTrue("Root path for ${gameType.title}: (not found)", rootPath != null && rootPath.isDirectory())
            rootPath!!
            println("Root path for ${gameType.title}: ${rootPath}")

            val executablePath = ParadoxGameManager.getExecutablePath(gameType, rootPath)
            Assert.assertNotNull("Executable path for ${gameType.title}: (unknown)", executablePath)
            executablePath!!
            println("Executable path for ${gameType.title}: ${executablePath}")
        }

        @Test
        fun getBranchPath() {
            val service = PlsPathService.getInstance()

            val rootPath = service.getSteamGamePath(gameType.steamId, gameType.title)
            Assume.assumeTrue("Root path for ${gameType.title}: (not found)", rootPath != null && rootPath.isDirectory())
            rootPath!!
            println("Root path for ${gameType.title}: ${rootPath}")

            val branchPath = ParadoxGameManager.getBranchPath(gameType, rootPath)
            Assume.assumeTrue("Branch path for ${gameType.title}: (not found)", branchPath != null && rootPath.isRegularFile())
            branchPath!!
            println("Branch path for ${gameType.title}: ${branchPath}")
        }
    }
}
