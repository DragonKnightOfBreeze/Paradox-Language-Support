package icu.windea.pls.images

import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.images.supports.DefaultImageSupport
import icu.windea.pls.integrations.ExternalToolDownloadTestService
import icu.windea.pls.integrations.images.ImageToolProvider
import icu.windea.pls.integrations.images.ImageToolService
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
 * @see DefaultImageSupport
 * @see TexconvToolProvider
 * @see MagickToolProvider
 */
@RunWith(JUnit4::class)
class ImageFormatConversionTest : BasePlatformTestCase() {
    private val formats = listOf("jpg", "png", "dds", "tga")

    private val imageState get() = ChronicleIntegrationsSettings.getInstance().state.image
    private var originalEnableTexconv = false
    private var originalTexconvPath: String? = null
    private var originalEnableMagick = false
    private var originalMagickPath: String? = null

    @Before
    fun setUpSettings() {
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
    fun defaultStrategy_matrix_test() {
        val support = DefaultImageSupport()
        for (sourceFormat in formats) {
            for (targetFormat in formats) {
                val source = sourcePath(sourceFormat)
                val target = targetPath("default-$sourceFormat-to-$targetFormat.$targetFormat")
                val expected = isSupportedByDefault(sourceFormat, targetFormat)
                val result = runCatching { support.convertImageFormat(source, target, sourceFormat, targetFormat) }
                Assert.assertEquals("default: $sourceFormat -> $targetFormat", expected, result.isSuccess)
                if (expected) assertConverted(target) else assertFailure(target)
            }
        }
    }

    @Test
    fun texconvStrategy_matrix_test() {
        ChronicleAssume.includeExternalTools()
        Assume.assumeTrue("Texconv is Windows-only", SystemInfo.isWindows)
        val descriptor = ExternalToolDownloadTestService.texconv ?: error("Texconv descriptor is missing")
        val exePath = ExternalToolDownloadTestService.ensureTool(descriptor)
        imageState.enableTexconv = true
        imageState.texconvPath = exePath.toString()
        val provider = ImageToolProvider.EP_NAME.findExtension(TexconvToolProvider::class.java) ?: error("TexconvToolProvider not found")
        Assume.assumeTrue("Texconv is not available", provider.isAvailable())
        assertMatrix(provider, "texconv")
    }

    @Test
    fun magickStrategy_matrix_test() {
        ChronicleAssume.includeExternalTools()
        Assume.assumeTrue("ImageMagick is not tested on this OS", SystemInfo.isWindows || SystemInfo.isLinux)
        val descriptor = ExternalToolDownloadTestService.magick ?: error("ImageMagick descriptor is missing")
        val exePath = ExternalToolDownloadTestService.ensureTool(descriptor)
        imageState.enableMagick = true
        imageState.magickPath = exePath.toString()
        val provider = ImageToolProvider.EP_NAME.findExtension(MagickToolProvider::class.java) ?: error("MagickToolProvider not found")
        Assume.assumeTrue("ImageMagick is not available", provider.isAvailable())
        assertMatrix(provider, "magick")
    }

    @Test
    fun noOpSameFormat_test() {
        for (format in formats) {
            val source = sourcePath(format)
            val target = targetPath("noop-$format.$format")
            ImageService.getInstance().convertImageFormat(source, target, format, format)
            Assert.assertArrayEquals("no-op $format", Files.readAllBytes(source), Files.readAllBytes(target))
        }
    }

    @Test
    fun noOpToolServiceInEdt_test() {
        val source = sourcePath("jpg")
        val target = targetPath("noop-service.jpg")
        val converted = ImageToolService.getInstance().convertImageFormat(source, target, "jpg", "jpg")
        Assert.assertTrue(converted)
        Assert.assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(target))
    }

    @Test
    fun noOpToolProvider_test() {
        imageState.enableTexconv = true
        imageState.texconvPath = "Z:\\not-existing\\texconv.exe"
        val provider = TexconvToolProvider()
        val source = sourcePath("jpg")
        val target = targetPath("noop-provider.jpg")
        provider.convertImageFormat(source, target, "jpg", "jpg")
        Assert.assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(target))
    }

    private fun assertMatrix(provider: ImageToolProvider, strategy: String) {
        for (sourceFormat in formats) {
            for (targetFormat in formats) {
                val source = sourcePath(sourceFormat)
                val target = targetPath("$strategy-$sourceFormat-to-$targetFormat.$targetFormat")
                val result = runCatching { provider.convertImageFormat(source, target, sourceFormat, targetFormat) }
                Assert.assertTrue("$strategy: $sourceFormat -> $targetFormat, error=${result.exceptionOrNull()}", result.isSuccess)
                assertConverted(target)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun isSupportedByDefault(sourceFormat: String, targetFormat: String): Boolean {
        // 同格式直接复制；默认实现没有 DDS 写入器
        return sourceFormat == targetFormat || targetFormat != "dds"
    }

    private fun sourcePath(format: String): Path {
        return Path.of("src", "test", "testData", "images", "mod_unknown.$format").toAbsolutePath().normalize()
    }

    private fun targetPath(fileName: String): Path {
        val dir = Path.of("build", "tmp", "image-format-conversion-test").toAbsolutePath().normalize().also { it.createDirectories() }
        return dir.resolve(fileName).also { it.deleteIfExists() }
    }

    private fun assertConverted(target: Path) {
        Assert.assertTrue("Missing converted image: $target", Files.isRegularFile(target))
        Assert.assertTrue("Empty converted image: $target", Files.size(target) > 0)
    }

    private fun assertFailure(target: Path) {
        Assert.assertFalse("Unexpected converted image: $target", Files.isRegularFile(target) && Files.size(target) > 0)
    }
}
