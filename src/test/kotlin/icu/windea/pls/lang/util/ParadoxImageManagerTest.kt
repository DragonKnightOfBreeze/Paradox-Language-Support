package icu.windea.pls.lang.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.images.ImageRenderRequest
import icu.windea.pls.images.ImageRenderService
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.lang.resolve.ParadoxImageService
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.model.policies.ImageRenderPolicy
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
 * @see ParadoxImageManager
 * @see ParadoxImageService
 */
@RunWith(JUnit4::class)
class ParadoxImageManagerTest : BasePlatformTestCase() {
    private val root = Path.of("build/tmp/paradox-image-manager-test")

    @Before
    fun doSetUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @Test
    fun isImageFile_test() {
        val imagePath = Path.of("src/test/testData/images/mod_unknown.png")
        val imageFile = VfsUtil.findFileByIoFile(imagePath.toFile(), true) ?: error("Cannot find file: $imagePath")
        Assert.assertTrue(ParadoxImageManager.isImageFile(imageFile))

        val textPath = root.resolve("not-image.txt")
        Files.writeString(textPath, "text")
        val textFile = VfsUtil.findFileByIoFile(textPath.toFile(), true) ?: error("Cannot find file: $textPath")
        Assert.assertFalse(ParadoxImageManager.isImageFile(textFile))
    }

    @Test
    fun canResolve_test() {
        val imagePath = root.resolve("can-resolve.png")
        writeImage(imagePath, 10, 10)
        Assert.assertTrue(ParadoxImageManager.canResolve(imagePath.absolutePathString()))
        Assert.assertFalse(ParadoxImageManager.canResolve(root.resolve("missing.png").absolutePathString()))
        Assert.assertFalse(ParadoxImageManager.canResolve(null as String?))
    }

    @Test
    fun resolveUrl_noRenderRequired_test() {
        val source = root.resolve("no-render.png")
        writeImage(source, 10, 10)
        val result = ParadoxImageResolveResult(source, source)

        val url = ParadoxImageManager.resolveUrl(result, project)

        Assert.assertEquals(source.absolutePathString(), url)
    }

    @Test
    fun resolveUrl_cachedPath_test() {
        val source = root.resolve("cached-source.png")
        val target = root.resolve("cached-target.png")
        writeImage(source, 10, 10)
        writeImage(target, 10, 10)
        val result = ParadoxImageResolveResult(source, target)

        val url = ParadoxImageManager.resolveUrl(result, project)

        Assert.assertEquals(target.absolutePathString(), url)
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
        val source = root.resolve("async-source.png")
        val target = root.resolve("async-target.png")
        writeImage(source, 10, 10)

        val result = ParadoxImageResolveResult(source, target)
        val url = ParadoxImageManager.resolveUrl(result, project, ImageRenderPolicy.Async)
        Assert.assertNull(url)

        waitForFile(target)
    }

    @Test
    fun resolveUrl_usesSettingsRenderPolicy_test() {
        val source = root.resolve("settings-async-source.png")
        val target = root.resolve("settings-async-target.png")
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
        waitForFile(target)
    }

    @Test
    fun resolveUrl_explicitSyncOverridesSettings_test() {
        val source = root.resolve("sync-source.png")
        val target = root.resolve("sync-target.png")
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

    private fun createBrokenRequest(name: String): ImageRenderRequest {
        val source = root.resolve("$name-source.png")
        val target = root.resolve("$name-target.png")
        Files.writeString(source, "not an image")
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

    private fun waitForFile(path: Path) {
        val deadline = System.currentTimeMillis() + 10_000
        while (!Files.isRegularFile(path) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        Assert.assertTrue("Image rendering should eventually finish: $path", Files.isRegularFile(path))
    }

    @Suppress("SameParameterValue")
    private fun writeImage(path: Path, width: Int, height: Int) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "png", path.toFile())
    }
}
