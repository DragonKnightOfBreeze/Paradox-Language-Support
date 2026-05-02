package icu.windea.pls.lang.analysis

import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import kotlin.io.path.isDirectory

class ParadoxAnalysisUtilTest {
    @Test
    fun compareGameVersion() {
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.3") == 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.4") < 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.2") > 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.3.") == 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.3.0") < 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.3.1") < 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3.0", "3.3.1") < 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3.0", "3.3.*") == 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.3.*") == 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.4.*") < 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.3", "3.2.*") > 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.14", "3.6") > 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.14", "3.6.1") > 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.99.1 beta", "3.99.1 beta") == 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.99.1 beta", "3.99.1  beta") == 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.99.1", "3.99.1 beta") > 0)
        Assert.assertTrue(ParadoxAnalysisUtil.compareGameVersion("3.99.8 beta", "3.99.1 beta") > 0)
    }

    @Test
    fun getExecutablePath() {
        val gameTypes = ParadoxGameType.getAll()
        for (gameType in gameTypes) {
            assertExecutablePath(gameType)
        }
    }

    private fun assertExecutablePath(gameType: ParadoxGameType) {
        val service = PlsPathService.getInstance()
        val rootPath = service.getSteamGamePath(gameType.steamId, gameType.title)
        Assume.assumeTrue("root path for ${gameType.title} is missing", rootPath != null && rootPath.isDirectory())
        rootPath!!
        val executablePath = ParadoxAnalysisUtil.getExecutablePath(gameType, rootPath)
        Assert.assertNotNull(executablePath)
        executablePath!!
        println("Executable path for ${gameType.title}: ${executablePath}")
    }
}
