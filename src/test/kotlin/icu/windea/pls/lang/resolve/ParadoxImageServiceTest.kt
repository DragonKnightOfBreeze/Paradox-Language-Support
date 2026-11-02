package icu.windea.pls.lang.resolve

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.util.ImageFrameInfo
import icu.windea.pls.images.ImageRenderRequest
import icu.windea.pls.images.ImageRenderService
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.test.ChronicleTestScope
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
 * @see ParadoxImageService
 * @see ParadoxImageResolveResult
 */
@RunWith(JUnit4::class)
class ParadoxImageServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    private val root = Path.of("build/tmp/paradox-image-service-test")

    @Before
    fun doSetUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
        addAdditionalAllowedRoots(Path.of("src", "test", "testData"))
    }

    @Test
    fun isImageFile_test() {
        val imagePath = Path.of("src/test/testData/images/mod_unknown.png")
        val imageFile = VfsUtil.findFileByIoFile(imagePath.toFile(), true) ?: error("Cannot find file: $imagePath")
        Assert.assertTrue(ParadoxImageService.isImageFile(imageFile))

        val textPath = root.resolve("not-image.txt")
        Files.writeString(textPath, "text")
        val textFile = VfsUtil.findFileByIoFile(textPath.toFile(), true) ?: error("Cannot find file: $textPath")
        Assert.assertFalse(ParadoxImageService.isImageFile(textFile))
    }

    @Test
    fun resolveByFile_png_test() {
        val path = Path.of("src/test/testData/images/mod_unknown.png")
        val file = findFile(path)

        val result = ParadoxImageService.resolveByFile(file)

        Assert.assertNotNull(result)
        Assert.assertEquals(path.toAbsolutePath(), result!!.sourcePath.toAbsolutePath())
        Assert.assertEquals(path.toAbsolutePath(), result.targetPath.toAbsolutePath())
        Assert.assertFalse(result.renderRequired)
        Assert.assertNull(result.frameInfo)
    }

    @Test
    fun resolveByFile_dds_test() {
        assertResolveByFileWithoutFrame("src/test/testData/images/mod_unknown.dds")
    }

    @Test
    fun resolveByFile_tga_test() {
        assertResolveByFileWithoutFrame("src/test/testData/images/mod_unknown.tga")
    }

    @Test
    fun resolveByFile_withFrameInfo_test() {
        val path = Path.of("src/test/testData/images/mod_unknown.png")
        val file = findFile(path)
        val frameInfo = ImageFrameInfo.of(1, 2)

        val result = ParadoxImageService.resolveByFile(file, frameInfo)

        Assert.assertNotNull(result)
        Assert.assertTrue(result!!.renderRequired)
        Assert.assertEquals(frameInfo, result.frameInfo)
        Assert.assertNotEquals(path.toAbsolutePath(), result.targetPath.toAbsolutePath())
        Assert.assertTrue(result.targetPath.fileName.toString().endsWith(".png"))
        Assert.assertTrue(result.targetPath.fileName.toString().contains("@1_2"))
    }

    @Test
    fun resolveByFile_notImage_test() {
        val path = root.resolve("not-image.txt")
        Files.createDirectories(path.parent)
        Files.writeString(path, "text")
        val file = findFile(path)

        Assert.assertNull(ParadoxImageService.resolveByFile(file))
    }

    private fun assertResolveByFileWithoutFrame(filePath: String) {
        val path = Path.of(filePath)
        val file = findFile(path)

        val result = ParadoxImageService.resolveByFile(file)

        Assert.assertNotNull(result)
        Assert.assertEquals(path.toAbsolutePath(), result!!.sourcePath.toAbsolutePath())
        Assert.assertEquals(path.toAbsolutePath(), result.targetPath.toAbsolutePath())
        Assert.assertFalse(result.renderRequired)
    }

    private fun findFile(path: Path) = VfsUtil.findFileByIoFile(path.toFile(), true) ?: error("Cannot find file: $path")

    @Test
    fun canResolve_test() {
        val imagePath = root.resolve("can-resolve.png")
        writeImage(imagePath, 10, 10)
        Assert.assertTrue(ParadoxImageService.canResolve(imagePath.absolutePathString()))
        Assert.assertFalse(ParadoxImageService.canResolve(root.resolve("missing.png").absolutePathString()))
    }

    @Test
    fun resolveUrl_noRenderRequired_test() {
        val source = root.resolve("no-render.png")
        writeImage(source, 10, 10)
        val result = ParadoxImageResolveResult(source, source)
        val url = result.resolveUrl(project)
        Assert.assertEquals(source.absolutePathString(), url)
    }

    @Test
    fun resolveUrl_cachedPath_test() {
        val source = root.resolve("cached-source.png")
        val target = root.resolve("cached-target.png")
        writeImage(source, 10, 10)
        writeImage(target, 10, 10)
        val result = ParadoxImageResolveResult(source, target)
        val url = result.resolveUrl(project)
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

    private fun createBrokenRequest(name: String): ImageRenderRequest {
        val source = root.resolve("$name-source.png")
        val target = root.resolve("$name-target.png")
        Files.writeString(source, "not an image")
        Files.deleteIfExists(target)
        return ImageRenderRequest(source, target)
    }

    private fun resolveUrlOnPooledThread(result: ParadoxImageResolveResult): String? {
        return ApplicationManager.getApplication()
            .executeOnPooledThread<String?> { result.resolveUrl(project) }
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

    @Suppress("SameParameterValue")
    private fun writeImage(path: Path, width: Int, height: Int) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "png", path.toFile())
    }
}
