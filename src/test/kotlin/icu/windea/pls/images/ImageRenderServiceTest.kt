package icu.windea.pls.images

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.codeInsight.documentation.buildDocumentation
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
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
    private val root = Path.of("build/tmp/image-render-service-test")

    @Before
    fun doSetUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
        // 注册内置的 DDS/TGA ImageIO SPI，确保测试不依赖插件启动生命周期
        ImageContext.init()
    }

    @Test
    fun renderImage_test() {
        val dir = root
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
        Assert.assertTrue(metadata.width <= 5)
        Assert.assertTrue(metadata.height <= 5)
    }

    @Test
    fun requestImageRender_test() {
        val dir = root
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
    fun renderImageIcon_test()  {
        val source = root.resolve("icon-source.png")
        val target = root.resolve("icon-target.png")
        writeImage(source, 10, 10)

        val icon = ImageRenderService.getInstance().renderImageIcon(ImageRenderRequest(source, target), maxHeight = 36)

        Assert.assertNotNull(icon)
        Assert.assertEquals(10, icon!!.iconWidth)
        Assert.assertEquals(10, icon.iconHeight)
    }

    @Test
    fun renderImageIcon_heightLimit_test() {
        val source = root.resolve("icon-too-tall.png")
        val target = root.resolve("icon-too-tall-target.png")
        writeImage(source, 10, 10)

        Assert.assertNull(ImageRenderService.getInstance().renderImageIcon(ImageRenderRequest(source, target), maxHeight = 5))
    }

    @Test
    fun renderSession_test() = runBlocking {
        val dir = root
        val source = dir.resolve("session-source.png")
        val target = dir.resolve("session-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val service = ImageRenderService.getInstance()
        val session = service.newRenderSession(this)
        val result = service.withRenderSession(session) {
            ParadoxImageResolveResult(source, target).resolveUrl(project)
        }

        Assert.assertEquals(target.absolutePathString(), result)
        Assert.assertEquals(listOf(target), session.awaitAll())
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImagePath(ImageRenderRequest(source, target)))
    }

    @Test
    fun applyFailurePolicy_acceptBroken_test() {
        runBlocking {
            val request = createBrokenRequest("session-accept-broken")
            val service = ImageRenderService.getInstance()
            val session = service.newRenderSession(this)
            session.launch(request).await()
            val builder = buildDocumentation { image(request.targetPath.absolutePathString()) }

            withRenderFailurePolicy(ImageRenderFailurePolicy.AcceptBroken) {
                service.applyFailurePolicy(builder, session)
            }

            Assert.assertTrue(builder.content.contains(request.targetPath.absolutePathString().toFileUrl().toString()))
        }
    }

    @Test
    fun applyFailurePolicy_removeImage_test() {
        runBlocking {
            val request = createBrokenRequest("session-remove-image")
            val service = ImageRenderService.getInstance()
            val session = service.newRenderSession(this)
            session.launch(request).await()
            val builder = buildDocumentation { image(request.targetPath.absolutePathString()) }

            withRenderFailurePolicy(ImageRenderFailurePolicy.RemoveImage) {
                service.applyFailurePolicy(builder, session)
            }

            Assert.assertFalse(builder.content.contains(request.targetPath.absolutePathString().toFileUrl().toString()))
        }
    }

    @Test
    fun applyFailurePolicy_placeholder_test() {
        runBlocking {
            val request = createBrokenRequest("session-placeholder")
            val service = ImageRenderService.getInstance()
            val session = service.newRenderSession(this)
            session.launch(request).await()
            val builder = buildDocumentation { image(request.targetPath.absolutePathString()) }

            withRenderFailurePolicy(ImageRenderFailurePolicy.Placeholder) {
                service.applyFailurePolicy(builder, session)
            }

            val placeholderUrl = service.getPlaceholderImagePath().absolutePathString().toFileUrl().toString()
            Assert.assertTrue(builder.content.contains(placeholderUrl))
        }
    }

    private fun createBrokenRequest(name: String): ImageRenderRequest {
        val dir = root
        val source = dir.resolve("$name-source.png")
        val target = dir.resolve("$name-target.png")
        Files.write(source, "not an image".toByteArray())
        Files.deleteIfExists(target)
        return ImageRenderRequest(source, target)
    }


    private fun <T> withRenderFailurePolicy(policy: ImageRenderFailurePolicy, block: () -> T): T {
        val settings = ChronicleImageSettings.getInstance().state
        val oldPolicy = settings.renderFailurePolicy
        settings.renderFailurePolicy = policy
        try {
            return block()
        } finally {
            settings.renderFailurePolicy = oldPolicy
        }
    }

    @Suppress("SameParameterValue")
    private fun writeImage(path: Path, width: Int, height: Int) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "png", path.toFile())
    }
}
