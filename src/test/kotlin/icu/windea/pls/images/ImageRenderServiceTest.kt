package icu.windea.pls.images

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.lang.util.ParadoxImageManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

/**
 * @see ImageRenderService
 * @see ParadoxImageManager
 */
@RunWith(JUnit4::class)
class ImageRenderServiceTest : BasePlatformTestCase() {
    @Test
    fun renderImage_test() {
        val dir = testDirectory()
        val source = dir.resolve("source.png")
        val target = dir.resolve("target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val request = ImageRenderRequest(source, target, maxWidth = 5, maxHeight = 5)
        val service = ImageRenderService.getInstance()
        val result = service.renderImage(request)

        Assert.assertEquals(target, result)
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImagePath(request))
        val metadata = ImageMetadataService.getInstance().getMetadata(target)
        Assert.assertNotNull(metadata)
        Assert.assertTrue(metadata!!.width <= 5)
        Assert.assertTrue(metadata.height <= 5)
    }

    @Test
    fun requestImageRender_test() {
        val dir = testDirectory()
        val source = dir.resolve("request-source.png")
        val target = dir.resolve("request-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val request = ImageRenderRequest(source, target)
        val service = ImageRenderService.getInstance()
        Assert.assertNull(service.getCachedImagePath(request))

        // `requestImageRender` 需要在后台线程执行
        val result = ApplicationManager.getApplication()
            .executeOnPooledThread<Path?> { service.requestImageRender(request, project) }
            .get()

        Assert.assertEquals(target, result)
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImagePath(request))
    }

    @Test
    fun renderSession_test() = runBlocking {
        val dir = testDirectory()
        val source = dir.resolve("session-source.png")
        val target = dir.resolve("session-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val service = ImageRenderService.getInstance()
        val session = service.newRenderSession(this)
        val result = service.withRenderSession(session) {
            ParadoxImageManager.resolveUrl(ParadoxImageResolveResult(source, target), project)
        }

        Assert.assertEquals(target.absolutePathString(), result)
        Assert.assertEquals(listOf(target), session.awaitAll())
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImagePath(ImageRenderRequest(source, target)))
    }

    private fun testDirectory(): Path {
        return Path.of("build", "tmp", "image-render-service-test").also { it.createDirectories() }
    }

    @Suppress("SameParameterValue")
    private fun writeImage(path: Path, width: Int, height: Int) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "png", path.toFile())
    }
}
