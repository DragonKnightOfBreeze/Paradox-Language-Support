package icu.windea.pls.images

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.codeInsight.documentation.buildDocumentation
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.model.policies.ImageRenderPolicy
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

    @Test
    fun resolveUrl_acceptBrokenFailurePolicy_test() {
        val request = createBrokenRequest("accept-broken")
        val result = ParadoxImageResolveResult(request.sourcePath, request.targetPath)

        val url = withRenderFailurePolicy(ImageRenderFailurePolicy.AcceptBroken) {
            resolveUrlOnPooledThread(result)
        }

        Assert.assertEquals(request.targetPath.absolutePathString(), url)
    }

    @Test
    fun resolveUrl_removeImageFailurePolicy_test() {
        val request = createBrokenRequest("remove-image")
        val result = ParadoxImageResolveResult(request.sourcePath, request.targetPath)

        val url = withRenderFailurePolicy(ImageRenderFailurePolicy.RemoveImage) {
            resolveUrlOnPooledThread(result)
        }

        Assert.assertNull(url)
    }

    @Test
    fun resolveUrl_placeholderFailurePolicy_test() {
        val request = createBrokenRequest("placeholder")
        val result = ParadoxImageResolveResult(request.sourcePath, request.targetPath)

        val url = withRenderFailurePolicy(ImageRenderFailurePolicy.Placeholder) {
            resolveUrlOnPooledThread(result)
        }

        Assert.assertEquals(ImageRenderService.getInstance().getPlaceholderImagePath().absolutePathString(), url)
        Assert.assertTrue(Files.isRegularFile(ImageRenderService.getInstance().getPlaceholderImagePath()))
    }

    @Test
    fun resolveUrl_asyncRenderPolicy_test() {
        val dir = testDirectory()
        val source = dir.resolve("async-source.png")
        val target = dir.resolve("async-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val result = ParadoxImageResolveResult(source, target)
        val url = ParadoxImageManager.resolveUrl(result, project, ImageRenderPolicy.Async)
        Assert.assertNull(url)

        val deadline = System.currentTimeMillis() + 10_000
        while (!Files.isRegularFile(target) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        Assert.assertTrue("Async image rendering should eventually finish", Files.isRegularFile(target))
    }

    @Test
    fun resolveUrl_usesSettingsRenderPolicy_test() {
        val dir = testDirectory()
        val source = dir.resolve("settings-async-source.png")
        val target = dir.resolve("settings-async-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val settings = ChronicleImageSettings.getInstance().state
        val oldPolicy = settings.renderPolicy
        settings.renderPolicy = ImageRenderPolicy.Async
        try {
            val url = ParadoxImageManager.resolveUrl(ParadoxImageResolveResult(source, target), project)
            Assert.assertNull(url)
        } finally {
            settings.renderPolicy = oldPolicy
        }
        val deadline = System.currentTimeMillis() + 10_000
        while (!Files.isRegularFile(target) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        Assert.assertTrue("Settings-based async rendering should eventually finish", Files.isRegularFile(target))
    }

    @Test
    fun resolveUrl_explicitSyncOverridesSettings_test() {
        val dir = testDirectory()
        val source = dir.resolve("sync-source.png")
        val target = dir.resolve("sync-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val result = ParadoxImageResolveResult(source, target)
        val settings = ChronicleImageSettings.getInstance().state
        val oldPolicy = settings.renderPolicy
        settings.renderPolicy = ImageRenderPolicy.Async
        try {
            val url = resolveUrlOnPooledThread(result, ImageRenderPolicy.Sync)
            Assert.assertEquals(target.absolutePathString(), url)
            Assert.assertTrue(Files.isRegularFile(target))
        } finally {
            settings.renderPolicy = oldPolicy
        }
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
        val dir = testDirectory()
        val source = dir.resolve("$name-source.png")
        val target = dir.resolve("$name-target.png")
        Files.write(source, "not an image".toByteArray())
        Files.deleteIfExists(target)
        return ImageRenderRequest(source, target)
    }

    private fun resolveUrlOnPooledThread(result: ParadoxImageResolveResult, policy: ImageRenderPolicy? = null): String? {
        return ApplicationManager.getApplication()
            .executeOnPooledThread<String?> { ParadoxImageManager.resolveUrl(result, project, policy) }
            .get()
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

    private fun testDirectory(): Path {
        return Path.of("build", "tmp", "image-render-service-test").also { it.createDirectories() }
    }

    @Suppress("SameParameterValue")
    private fun writeImage(path: Path, width: Int, height: Int) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "png", path.toFile())
    }
}
