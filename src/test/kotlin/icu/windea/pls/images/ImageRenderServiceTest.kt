package icu.windea.pls.images

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories

/**
 * @see ImageRenderService
 */
@RunWith(JUnit4::class)
class ImageRenderServiceTest : BasePlatformTestCase() {
    @Test
    fun renderImage_test() {
        val dir = Path.of("build", "tmp", "image-render-service-test").also { it.createDirectories() }
        val source = dir.resolve("source.png")
        val target = dir.resolve("target.png")
        Files.deleteIfExists(target)
        val sourceImage = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(sourceImage, "png", source.toFile())

        val request = ImageRenderRequest(source, target, maxWidth = 5, maxHeight = 5)
        val service = ImageRenderService.getInstance()
        val result = service.renderImage(request)

        Assert.assertEquals(target, result)
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImage(request))
        val metadata = ImageMetadataService.getInstance().getMetadata(target)
        Assert.assertNotNull(metadata)
        Assert.assertTrue(metadata!!.width <= 5)
        Assert.assertTrue(metadata.height <= 5)
    }
}
