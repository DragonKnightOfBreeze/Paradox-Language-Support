package icu.windea.pls.images

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.images.supports.DefaultImageSupport
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * @see ImageService
 */
@RunWith(JUnit4::class)
class ImageServiceTest : BasePlatformTestCase() {
    private val root = Path.of("build/tmp/image-service-test")

    @Before
    fun doSetUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
        // 注册内置的 DDS/TGA ImageIO SPI，确保测试不依赖插件启动生命周期
        ImageContext.init()
    }

    @Test
    fun convertImageFormat_pngToTga_test() {
        val source = Path.of("src/test/testData/images/mod_unknown.png")
        val target = root.resolve("mod_unknown.tga")
        Files.deleteIfExists(target)

        ImageService.getInstance().convertImageFormat(source, target, "png", "tga")

        assertValidImage(target, source)
    }

    @Test
    fun convertImageFormat_toTga_test() {
        val formats = listOf("jpg", "png", "dds", "tga")
        val service = ImageService.getInstance()
        for (format in formats) {
            val source = Path.of("src/test/testData/images/mod_unknown.$format")
            val target = root.resolve("mod_unknown-from-$format.tga")
            Files.deleteIfExists(target)

            service.convertImageFormat(source, target, format, "tga")

            assertValidImage(target, source)
        }
    }

    @Test
    fun convertImageFormat_roundTrip_test() {
        val source = Path.of("src/test/testData/images/mod_unknown.png")
        val tga = root.resolve("round-trip.tga")
        val png = root.resolve("round-trip.png")
        Files.deleteIfExists(tga)
        Files.deleteIfExists(png)

        val service = ImageService.getInstance()
        service.convertImageFormat(source, tga, "png", "tga")
        service.convertImageFormat(tga, png, "tga", "png")

        assertValidImage(tga, source)
        assertValidImage(png, source)
    }

    @Test
    fun defaultConvertImageFormat_jpgToPng_test() {
        val source = Path.of("src/test/testData/images/mod_unknown.jpg")
        val target = root.resolve("default-from-jpg.png")
        Files.deleteIfExists(target)

        DefaultImageSupport().convertImageFormat(source, target, "jpg", "png")

        assertValidImage(target, source)
    }

    @Test
    fun convertImageFormat_standardFormats_test() {
        val formats = listOf("jpg", "png", "dds", "tga")
        val service = ImageService.getInstance()
        for (format in formats) {
            val source = Path.of("src/test/testData/images/mod_unknown.$format")
            val target = root.resolve("mod_unknown-from-$format.png")
            Files.deleteIfExists(target)

            service.convertImageFormat(source, target, format, "png")

            assertValidImage(target, source)
        }
    }

    private fun assertValidImage(target: Path, source: Path) {
        Assert.assertTrue("Missing converted image: $target", Files.isRegularFile(target))
        Assert.assertTrue("Empty converted image: $target", Files.size(target) > 0)
        val targetMetadata = ImageMetadataService.getInstance().getMetadata(target)
        val sourceMetadata = ImageMetadataService.getInstance().getMetadata(source)
        Assert.assertEquals("Unexpected width: $target", sourceMetadata.width, targetMetadata.width)
        Assert.assertEquals("Unexpected height: $target", sourceMetadata.height, targetMetadata.height)
    }
}
