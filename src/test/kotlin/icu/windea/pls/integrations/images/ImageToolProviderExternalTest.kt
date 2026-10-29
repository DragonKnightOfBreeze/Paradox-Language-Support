package icu.windea.pls.integrations.images

import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.images.ImageMetadataService
import icu.windea.pls.images.supports.DefaultImageSupport
import icu.windea.pls.integrations.ExternalToolDownloadTestService
import icu.windea.pls.integrations.images.providers.MagickToolProvider
import icu.windea.pls.integrations.images.providers.TexconvToolProvider
import icu.windea.pls.test.ChronicleAssume
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

/**
 * @see ImageToolProvider
 * @see TexconvToolProvider
 * @see MagickToolProvider
 * @see DefaultImageSupport
 */
@RunWith(JUnit4::class)
class ImageToolProviderExternalTest : BasePlatformTestCase() {
    private val imageState get() = ChronicleIntegrationsSettings.getInstance().state.image
    private var originalEnableTexconv = false
    private var originalTexconvPath: String? = null
    private var originalEnableMagick = false
    private var originalMagickPath: String? = null

    @Before
    fun setUpSettings() {
        ChronicleAssume.includeExternalTools()
        originalEnableTexconv = imageState.enableTexconv
        originalTexconvPath = imageState.texconvPath
        originalEnableMagick = imageState.enableMagick
        originalMagickPath = imageState.magickPath
        imageState.enableTexconv = false
        imageState.texconvPath = null
        imageState.enableMagick = false
        imageState.magickPath = null
    }

    @After
    fun restoreSettings() {
        imageState.enableTexconv = originalEnableTexconv
        imageState.texconvPath = originalTexconvPath
        imageState.enableMagick = originalEnableMagick
        imageState.magickPath = originalMagickPath
    }

    @Test
    fun defaultStrategy_jpgToPng_test() {
        val source = sourcePath("mod_unknown.jpg")
        val target = targetPath("default-from-jpg.png")
        DefaultImageSupport().convertImageFormat(source, target, "jpg", "png")
        assertValidImage(target, source)
    }

    @Test
    fun texconvStrategy_jpgToPng_test() {
        Assume.assumeTrue("Texconv is Windows-only", SystemInfo.isWindows)
        val descriptor = ExternalToolDownloadTestService.texconv ?: error("Texconv descriptor is missing")
        val texconvPath = ExternalToolDownloadTestService.ensureTool(descriptor)
        imageState.enableTexconv = true
        imageState.texconvPath = texconvPath.toString()
        val provider = ImageToolProvider.EP_NAME.findExtension(TexconvToolProvider::class.java) ?: error("TexconvToolProvider not found")
        Assume.assumeTrue("Texconv is not available", provider.isAvailable())

        val source = sourcePath("mod_unknown.jpg")
        val target = targetPath("texconv-from-jpg.png")
        provider.convertImageFormat(source, target, "jpg", "png")
        assertValidImage(target, source)
    }

    @Test
    fun magickStrategy_jpgToPng_test() {
        Assume.assumeTrue("ImageMagick is not tested on this OS", SystemInfo.isWindows || SystemInfo.isLinux)
        val descriptor = ExternalToolDownloadTestService.magick ?: error("ImageMagick descriptor is missing")
        val magickPath = ExternalToolDownloadTestService.ensureTool(descriptor)
        imageState.enableMagick = true
        imageState.magickPath = magickPath.toString()
        val provider = ImageToolProvider.EP_NAME.findExtension(MagickToolProvider::class.java) ?: error("MagickToolProvider not found")
        Assume.assumeTrue("ImageMagick is not available", provider.isAvailable())

        val source = sourcePath("mod_unknown.jpg")
        val target = targetPath("magick-from-jpg.png")
        provider.convertImageFormat(source, target, "jpg", "png")
        assertValidImage(target, source)
    }

    @Suppress("SameParameterValue")
    private fun sourcePath(fileName: String): Path {
        return Path.of("src", "test", "testData", "images", fileName).toAbsolutePath().normalize()
    }

    private fun targetPath(fileName: String): Path {
        val dir = Path.of("build", "tmp", "image-tool-provider-external-test").toAbsolutePath().normalize().also { it.createDirectories() }
        return dir.resolve(fileName).also { it.deleteIfExists() }
    }

    private fun assertValidImage(target: Path, source: Path) {
        Assert.assertTrue("Missing converted image: $target", Files.isRegularFile(target))
        Assert.assertTrue("Empty converted image: $target", Files.size(target) > 0)
        val targetMetadata = ImageMetadataService.getInstance().getMetadata(target)
        val sourceMetadata = ImageMetadataService.getInstance().getMetadata(source)
        Assert.assertNotNull("Cannot read metadata: $target", targetMetadata)
        Assert.assertNotNull("Cannot read metadata: $source", sourceMetadata)
        Assert.assertEquals("Unexpected width: $target", sourceMetadata!!.width, targetMetadata!!.width)
        Assert.assertEquals("Unexpected height: $target", sourceMetadata.height, targetMetadata.height)
    }
}
