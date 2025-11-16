package icu.windea.pls.lang.util

import org.junit.Assert
import org.junit.Test

class ParadoxAnalyzeManagerTest {
    @Test
    fun compareGameVersion() {
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.3") == 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.4") < 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.2") > 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.3.") == 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.3.0") < 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.3.1") < 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3.0", "3.3.1") < 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3.0", "3.3.*") == 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.3.*") == 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.4.*") < 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.3", "3.2.*") > 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.14", "3.6") > 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.14", "3.6.1") > 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.99.1 beta", "3.99.1 beta") == 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.99.1 beta", "3.99.1  beta") == 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.99.1", "3.99.1 beta") > 0)
        Assert.assertTrue(ParadoxAnalyzeManager.compareGameVersion("3.99.8 beta", "3.99.1 beta") > 0)
    }
}
