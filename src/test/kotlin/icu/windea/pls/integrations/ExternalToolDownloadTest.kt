package icu.windea.pls.integrations

import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.integrations.ExternalToolDownloadTestService.ck3Tiger
import icu.windea.pls.integrations.ExternalToolDownloadTestService.magick
import icu.windea.pls.integrations.ExternalToolDownloadTestService.texconv
import icu.windea.pls.test.ChronicleAssume
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files

/**
 * @see ExternalToolDownloadService
 * @see ExternalToolDownloadTestService
 */
@RunWith(JUnit4::class)
class ExternalToolDownloadTest : BasePlatformTestCase() {
    @Before
    fun checkExternalToolsEnabled() {
        ChronicleAssume.includeExternalTools()
    }

    @Test
    fun download_texconv_test() {
        Assume.assumeTrue("Texconv is Windows-only", SystemInfo.isWindows)
        val descriptor = texconv ?: error("Texconv descriptor is missing")
        val path = ExternalToolDownloadTestService.ensureTool(descriptor)
        Assert.assertTrue(Files.isRegularFile(path))
        Assert.assertTrue(Files.size(path) > 0)
        ExternalToolDownloadTestService.runSmokeTest(path, "--help")
    }

    @Test
    fun download_magick_test() {
        Assume.assumeTrue("ImageMagick download is not supported on this OS", SystemInfo.isWindows || SystemInfo.isLinux)
        val descriptor = magick ?: error("ImageMagick descriptor is missing")
        val path = ExternalToolDownloadTestService.ensureTool(descriptor)
        Assert.assertTrue(Files.isRegularFile(path))
        Assert.assertTrue(Files.size(path) > 0)
        ExternalToolDownloadTestService.runSmokeTest(path, "-version")
    }

    @Test
    fun download_ck3Tiger_test() {
        Assume.assumeTrue("Tiger download is not supported on this OS", SystemInfo.isWindows || SystemInfo.isLinux)
        val descriptor = ck3Tiger ?: error("ck3-tiger descriptor is missing")
        val path = ExternalToolDownloadTestService.ensureTool(descriptor)
        Assert.assertTrue(Files.isRegularFile(path))
        Assert.assertTrue(Files.size(path) > 0)
        ExternalToolDownloadTestService.runSmokeTest(path, "--version")
    }
}
