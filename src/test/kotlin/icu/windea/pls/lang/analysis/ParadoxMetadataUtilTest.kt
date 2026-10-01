package icu.windea.pls.lang.analysis

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.lang.analysis.util.ParadoxMetadataUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.io.path.toPath

/**
 * @see ParadoxMetadataUtil.detectLauncherPlaylistPositionIsInt
 */
@RunWith(JUnit4::class)
class ParadoxMetadataUtilTest : BasePlatformTestCase() {
    // V2 的 position 是带引号的、左侧补零的十六进制字符串（如 "0000001001"）。
    // 它可能只由十进制数字组成，因此不能仅凭 JsonPrimitive.intOrNull 判断类型，
    // 否则会被误判为 V3（position 为 Int）。
    @Test
    fun detectLauncherPlaylistPositionIsInt_forV2() {
        val path = "/tools/playlist_v2.json".toClasspathUrl().toURI().toPath()
        Assert.assertEquals(false, ParadoxMetadataUtil.detectLauncherPlaylistPositionIsInt(path))
    }

    @Test
    fun detectLauncherPlaylistPositionIsInt_forV3() {
        val path = "/tools/playlist_v3.json".toClasspathUrl().toURI().toPath()
        Assert.assertEquals(true, ParadoxMetadataUtil.detectLauncherPlaylistPositionIsInt(path))
    }
}
