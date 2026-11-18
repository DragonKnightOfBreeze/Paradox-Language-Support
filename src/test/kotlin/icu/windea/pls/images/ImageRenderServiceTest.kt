package icu.windea.pls.images

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.JBImageIcon
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.codeInsight.documentation.buildDocumentation
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.util.io.ImageIoService
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
import java.nio.file.attribute.FileTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.Icon
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

/**
 * @see ImageRenderService
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
        // 避免应用级服务的内存缓存跨测试互相影响
        ImageRenderService.getInstance().invalidateMemoryCache()
    }


    @Test
    fun applyImageScaling_textIcon_test() = runBlocking {
        val source = root.resolve("scaling-source.png")
        val target = root.resolve("scaling-target.png")
        writeImage(source, 100, 40)

        val request = ImageDescriptor(source, target)
        val service = ImageRenderService.getInstance()
        val session = service.newRenderSession(this)
        session.launch(request).await()
        val builder = buildDocumentation { image(target.absolutePathString()) }

        service.applyImageScaling(builder, session, textHeight = 20, textIconSizeLimit = 50)

        Assert.assertTrue(builder.content.contains("width=\"50\" height=\"20\""))
    }

    @Test
    fun applyImageScaling_largeImageKeepsOriginalSize_test() = runBlocking {
        val source = root.resolve("scaling-large-source.png")
        val target = root.resolve("scaling-large-target.png")
        writeImage(source, 100, 100)

        val request = ImageDescriptor(source, target)
        val service = ImageRenderService.getInstance()
        val session = service.newRenderSession(this)
        session.launch(request).await()
        val builder = buildDocumentation { image(target.absolutePathString()) }

        service.applyImageScaling(builder, session, textHeight = 20, textIconSizeLimit = 36)

        Assert.assertTrue(builder.content.contains("<img src=\"${target.absolutePathString().toFileUrl()}\"/>"))
    }
    @Test
    fun descriptorStrategy_test() {
        val source = root.resolve("strategy-source.png")
        val target = root.resolve("strategy-target.png")
        writeImage(source, 10, 10)

        // 目标路径与源路径一致时，直接使用源图片
        val direct = ImageDescriptor(source)
        Assert.assertEquals(ImageStrategy.Direct, direct.strategy)
        Assert.assertFalse(direct.renderRequired)
        Assert.assertEquals("png", direct.sourceFormat)
        Assert.assertEquals("png", direct.targetFormat)

        // 目标路径与源路径不一致时，需要经由渲染管道派生
        val delegated = ImageDescriptor(source, target)
        Assert.assertEquals(ImageStrategy.Delegated, delegated.strategy)
        Assert.assertTrue(delegated.renderRequired)
        Assert.assertEquals("png", delegated.targetFormat)
    }

    @Test
    fun renderImage_test() {
        val dir = root
        val source = dir.resolve("source.png")
        val target = dir.resolve("target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val request = ImageDescriptor(source, target)
        val service = ImageRenderService.getInstance()
        val result = service.renderImage(request)

        Assert.assertEquals(target, result)
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImagePath(request))
        val metadata = ImageMetadataService.getInstance().getMetadata(target)
        Assert.assertEquals(10, metadata.width)
        Assert.assertEquals(10, metadata.height)
    }

    @Test
    fun getCachedImagePath_brokenPngCache_isDeletedAndRebuilt_test() {
        val source = root.resolve("broken-cache-source.png")
        val target = root.resolve("broken-cache-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        // 写入有效 PNG 后截断末尾的 IEND 块，模拟损坏的缓存文件
        writeImage(target, 10, 10)
        val bytes = Files.readAllBytes(target)
        Assert.assertTrue(bytes.size > 24)
        Files.write(target, bytes.copyOf(bytes.size - 12))

        val request = ImageDescriptor(source, target)
        val service = ImageRenderService.getInstance()

        // 命中损坏缓存时应删除并返回 null
        Assert.assertNull(service.getCachedImagePath(request))
        Assert.assertFalse(Files.exists(target))

        // 之后应能重新渲染出合法缓存
        Assert.assertEquals(target, service.renderImage(request))
        Assert.assertTrue(ImageIoService.isValidImageFile(target, "png"))
    }

    @Test
    fun getCachedImagePath_delegatedTouch_test() {
        val source = root.resolve("touch-source.png")
        val target = root.resolve("touch-target.png")
        writeImage(source, 10, 10)
        writeImage(target, 10, 10)

        val service = ImageRenderService.getInstance()
        val request = ImageDescriptor(source, target)

        // 超过 1 天：命中缓存时更新 mtime
        val oldTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2))
        Files.setLastModifiedTime(target, oldTime)
        Assert.assertEquals(target, service.getCachedImagePath(request))
        val touchedTime = Files.getLastModifiedTime(target).toMillis()
        Assert.assertTrue(touchedTime > oldTime.toMillis())
        Assert.assertTrue(touchedTime >= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))

        // 1 天内：不刷新 mtime
        val recentTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))
        Files.setLastModifiedTime(target, recentTime)
        Assert.assertEquals(target, service.getCachedImagePath(request))
        Assert.assertEquals(recentTime.toMillis(), Files.getLastModifiedTime(target).toMillis())
    }

    @Test
    fun getCachedImagePath_directDoesNotTouch_test() {
        val source = root.resolve("direct-source.png")
        writeImage(source, 10, 10)
        val oldTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40))
        Files.setLastModifiedTime(source, oldTime)

        val request = ImageDescriptor(source)
        val service = ImageRenderService.getInstance()

        Assert.assertEquals(source, service.getCachedImagePath(request))
        // Direct 策略绝不修改源文件的 mtime
        Assert.assertEquals(oldTime.toMillis(), Files.getLastModifiedTime(source).toMillis())
    }

    @Test
    fun renderImage_noPartFileLeft_test() {
        val source = root.resolve("part-source.png")
        val target = root.resolve("part-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val request = ImageDescriptor(source, target)
        val service = ImageRenderService.getInstance()
        Assert.assertEquals(target, service.renderImage(request))

        val partFileCount = Files.list(root).use { stream ->
            stream.filter { it.fileName.toString().endsWith(".part") }.count()
        }
        Assert.assertEquals(0L, partFileCount)
    }

    @Test
    fun requestImageRender_test() {
        val dir = root
        val source = dir.resolve("request-source.png")
        val target = dir.resolve("request-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val request = ImageDescriptor(source, target)
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
    fun createImageIcon_test()  {
        val source = root.resolve("icon-source.png")
        val target = root.resolve("icon-target.png")
        writeImage(source, 10, 10)

        val icon = ImageRenderService.getInstance().createImageIcon(ImageDescriptor(source, target), heightLimit = 36)

        Assert.assertNotNull(icon)
        Assert.assertEquals(10, icon!!.iconWidth)
        Assert.assertEquals(10, icon.iconHeight)
    }

    @Test
    fun createImageIcon_heightLimit_test() {
        val source = root.resolve("icon-too-tall.png")
        val target = root.resolve("icon-too-tall-target.png")
        writeImage(source, 10, 10)

        Assert.assertNull(ImageRenderService.getInstance().createImageIcon(ImageDescriptor(source, target), heightLimit = 5))
    }

    @Test
    fun createImage_memoryCacheReuse_test() {
        val source = root.resolve("memory-source.png")
        val target = root.resolve("memory-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 12, 12)

        val service = ImageRenderService.getInstance()
        val descriptor = ImageDescriptor(source, target)
        val first = service.createImage(descriptor)
        Assert.assertNotNull(first)
        Assert.assertTrue(Files.isRegularFile(target))

        // 删除目标缓存文件后，仍应直接从内存缓存命中同一实例
        Files.delete(target)
        val second = service.createImage(descriptor)
        Assert.assertSame(first, second)
    }

    @Test
    fun createImageIcon_memoryCacheReuse_test() {
        val source = root.resolve("memory-icon-source.png")
        val target = root.resolve("memory-icon-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 12, 12)

        val service = ImageRenderService.getInstance()
        val descriptor = ImageDescriptor(source, target)
        val first = service.createImageIcon(descriptor)
        Assert.assertNotNull(first)
        Assert.assertTrue(first is JBImageIcon)

        // 删除目标缓存文件后，图片与图标都仍应命中内存缓存并复用同一实例
        Files.delete(target)
        val second = service.createImageIcon(descriptor)
        Assert.assertSame(first, second)
    }

    @Test
    fun createImage_sourceChangeInvalidatesCache_test() {
        val source = root.resolve("invalidating-source.png")
        writeImage(source, 10, 10)

        val service = ImageRenderService.getInstance()
        val descriptor = ImageDescriptor(source)
        val first = service.createImage(descriptor)
        Assert.assertNotNull(first)
        Assert.assertEquals(10, first!!.getWidth(null))

        // 变更源文件内容与修改时间后，缓存键应失效并重新解码出新实例
        writeImage(source, 20, 20)
        Files.setLastModifiedTime(source, FileTime.fromMillis(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2)))
        val second = service.createImage(descriptor)
        Assert.assertNotSame(first, second)
        Assert.assertNotNull(second)
        Assert.assertEquals(20, second!!.getWidth(null))
    }

    @Test
    fun createImageIcon_heightLimitAppliesOnCacheHit_test() {
        val source = root.resolve("icon-limit-source.png")
        val target = root.resolve("icon-limit-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 10, 10)

        val service = ImageRenderService.getInstance()
        val descriptor = ImageDescriptor(source, target)
        val icon = service.createImageIcon(descriptor)
        Assert.assertNotNull(icon)

        // 内存缓存命中时仍需遵守高度限制
        Assert.assertNull(service.createImageIcon(descriptor, heightLimit = 5))
        val reused = service.createImageIcon(descriptor, heightLimit = 20)
        Assert.assertSame(icon, reused)
    }

    @Test
    fun createImageIcon_concurrentRequestsShareSameIcon_test() {
        val source = root.resolve("concurrent-icon-source.png")
        val target = root.resolve("concurrent-icon-target.png")
        Files.deleteIfExists(target)
        writeImage(source, 200, 200)

        val service = ImageRenderService.getInstance()
        val descriptor = ImageDescriptor(source, target)
        val threadCount = 8
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        try {
            val futures = (1..threadCount).map {
                executor.submit<Icon?> {
                    startLatch.await()
                    service.createImageIcon(descriptor)
                }
            }
            startLatch.countDown()
            val icons = futures.map { it.get(30, TimeUnit.SECONDS) }
            Assert.assertTrue(icons.all { it != null })
            val first = icons.first()
            icons.forEach { Assert.assertSame(first, it) }
        } finally {
            executor.shutdownNow()
        }
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
            ImageDescriptor(source, target).resolveUrl(project)
        }

        Assert.assertEquals(target.absolutePathString(), result)
        Assert.assertEquals(listOf(target), session.awaitAll())
        Assert.assertTrue(Files.isRegularFile(target))
        Assert.assertEquals(target, service.getCachedImagePath(ImageDescriptor(source, target)))
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

    private fun createBrokenRequest(name: String): ImageDescriptor {
        val dir = root
        val source = dir.resolve("$name-source.png")
        val target = dir.resolve("$name-target.png")
        Files.write(source, "not an image".toByteArray())
        Files.deleteIfExists(target)
        return ImageDescriptor(source, target)
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
