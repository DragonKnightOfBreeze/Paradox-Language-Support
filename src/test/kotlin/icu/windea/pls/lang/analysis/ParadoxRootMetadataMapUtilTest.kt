package icu.windea.pls.lang.analysis

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.lang.analysis.util.ParadoxRootMetadataUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.io.path.toPath

/**
 * @see icu.windea.pls.lang.analysis.util.ParadoxRootMetadataUtil
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxRootMetadataMapUtilTest : BasePlatformTestCase() {
    @Test
    fun getLauncherSettingsJsonInfo() {
        val url = "/analysis/launcher-settings.test.json".toClasspathUrl()
        val path = url.toURI().toPath()
        val info = ParadoxRootMetadataUtil.getLauncherSettingsJsonInfo(path)
        Assert.assertNotNull(info)
        info!!
        Assert.assertNotNull(info.version?.orNull())
    }

    @Test
    fun getMetadataJsonInfo() {
        val url = "/analysis/metadata.test.json".toClasspathUrl()
        val path = url.toURI().toPath()
        val info = ParadoxRootMetadataUtil.getMetadataJsonInfo(path)
        Assert.assertNotNull(info)
        info!!
        Assert.assertNotNull(info.name.orNull())
        Assert.assertNotNull(info.version?.orNull())
    }

    @Test
    fun getDescriptorModInfo() {
        val url = "/analysis/descriptor.test.mod".toClasspathUrl()
        val path = url.toURI().toPath()
        val info = ParadoxRootMetadataUtil.getDescriptorModInfo(path)
        Assert.assertNotNull(info)
        info!!
        Assert.assertNotNull(info.name.orNull())
        Assert.assertNotNull(info.version?.orNull())
        Assert.assertNotNull(info.picture?.orNull())
        Assert.assertNotNull(info.tags.orNull())
        Assert.assertNotNull(info.supportedVersion?.orNull())
    }
}
