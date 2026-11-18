package icu.windea.pls.integrations.images

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.integrations.images.providers.CommandBasedImageToolProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile

/**
 * @see CommandBasedImageToolProvider
 */
@RunWith(JUnit4::class)
class CommandBasedImageToolProviderTest : BasePlatformTestCase() {
    private val root = Path.of("build", "tmp", "command-based-image-tool-provider-test")
    private lateinit var tempDir: Path
    private lateinit var toolCacheDir: Path
    private lateinit var provider: TestCommandBasedImageToolProvider

    @Before
    fun doSetUp() {
        root.toFile().deleteRecursively()
        tempDir = root.resolve("_tmp").also { it.createDirectories() }
        toolCacheDir = root.resolve("_tool").also { it.createDirectories() }
        provider = TestCommandBasedImageToolProvider(tempDir, toolCacheDir)
    }

    @Test
    fun read_sameContent_usesCache_test() {
        val content = "fake-dds-content".toByteArray()

        val first = provider.read(0, null, inputStreamOf(content), "dds", "png")
        val second = provider.read(0, null, inputStreamOf(content), "dds", "png")

        // 相同内容只应执行一次外部命令，且两次都返回独立可用的图片
        Assert.assertEquals(1, provider.executionCount)
        Assert.assertNotSame(first, second)
        Assert.assertEquals(4, first.width)
        Assert.assertEquals(4, second.width)
        assertDirectoryEmpty(tempDir)
    }

    @Test
    fun convertImageFormat_sameContent_usesCache_test() {
        val content = "fake-dds-content".toByteArray()

        val firstBytes = runBlocking { convert(ByteArrayInputStream(content)) }
        val secondBytes = runBlocking { convert(ByteArrayInputStream(content)) }

        Assert.assertEquals(1, provider.executionCount)
        Assert.assertTrue(firstBytes.isNotEmpty())
        Assert.assertArrayEquals(firstBytes, secondBytes)
        Assert.assertTrue(ImageIoService.isValidImageFile(findCacheFile(), "png"))
        assertDirectoryEmpty(tempDir)
    }

    @Test
    fun read_brokenCache_isRebuilt_test() {
        val content = "fake-dds-content".toByteArray()
        provider.read(0, null, inputStreamOf(content), "dds", "png")
        Assert.assertEquals(1, provider.executionCount)

        // 只保留前 10 字节，缺失 PNG IEND，模拟损坏的缓存文件
        val cacheFile = findCacheFile()
        val bytes = Files.readAllBytes(cacheFile)
        Assert.assertTrue(bytes.size > 10)
        Files.write(cacheFile, bytes.copyOf(10))
        Assert.assertFalse(ImageIoService.isValidImageFile(cacheFile, "png"))

        val image = provider.read(0, null, inputStreamOf(content), "dds", "png")

        // 自动识别非法缓存并删除重建
        Assert.assertEquals(2, provider.executionCount)
        Assert.assertEquals(4, image.width)
        Assert.assertTrue(ImageIoService.isValidImageFile(cacheFile, "png"))
        assertDirectoryEmpty(tempDir)
    }

    @Test
    fun read_changedContent_isRebuilt_test() {
        val content = "fake-dds-content".toByteArray()
        provider.read(0, null, inputStreamOf(content), "dds", "png")
        Assert.assertEquals(1, provider.executionCount)

        // 改动 1 字节后 SHA-256 改变，应重新执行外部命令
        val changed = content.copyOf()
        changed[0] = (changed[0].toInt() + 1).toByte()
        provider.read(0, null, inputStreamOf(changed), "dds", "png")

        Assert.assertEquals(2, provider.executionCount)
    }

    @Test
    fun read_cacheHit_touchesThrottled_test() {
        val content = "fake-dds-content".toByteArray()
        provider.read(0, null, inputStreamOf(content), "dds", "png")
        val cacheFile = findCacheFile()

        // 超过 1 天：命中缓存时更新 mtime
        val oldTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2))
        Files.setLastModifiedTime(cacheFile, oldTime)
        provider.read(0, null, inputStreamOf(content), "dds", "png")
        val touchedTime = Files.getLastModifiedTime(cacheFile).toMillis()
        Assert.assertTrue(touchedTime > oldTime.toMillis())
        Assert.assertTrue(touchedTime >= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
        Assert.assertEquals(1, provider.executionCount)

        // 1 天内：不刷新 mtime
        val recentTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))
        Files.setLastModifiedTime(cacheFile, recentTime)
        provider.read(0, null, inputStreamOf(content), "dds", "png")
        Assert.assertEquals(recentTime.toMillis(), Files.getLastModifiedTime(cacheFile).toMillis())
        Assert.assertEquals(1, provider.executionCount)
    }

    private suspend fun convert(inputStream: ByteArrayInputStream): ByteArray {
        return ByteArrayOutputStream().also { outputStream ->
            provider.convertImageFormat(inputStream, outputStream, "dds", "png")
        }.toByteArray()
    }

    private fun inputStreamOf(bytes: ByteArray): MemoryCacheImageInputStream {
        return MemoryCacheImageInputStream(ByteArrayInputStream(bytes))
    }

    private fun findCacheFile(): Path {
        val stream = Files.walk(toolCacheDir)
        try {
            return stream.filter { it.isRegularFile() }.findFirst()
                .orElseThrow { AssertionError("Cache file not found under $toolCacheDir") }
        } finally {
            stream.close()
        }
    }

    private fun assertDirectoryEmpty(dir: Path) {
        val entries = Files.newDirectoryStream(dir).use { stream -> stream.map { it.fileName.toString() } }
        Assert.assertTrue("Unexpected temp files: $entries", entries.isEmpty())
    }
}

private class TestCommandBasedImageToolProvider(
    private val tempDirectory: Path,
    private val toolCacheDirectory: Path,
) : CommandBasedImageToolProvider() {
    override val name = "test"

    var executionCount = 0

    override fun isEnabled(): Boolean = true

    override fun isSupported(): Boolean = true

    override fun getExecutablePath(): Path = toolCacheDirectory.resolve("test.exe")

    override fun validateExecutablePath(path: Path): Boolean = true

    override fun getTempDirectory(): Path = tempDirectory

    override fun getToolCacheDirectory(): Path = toolCacheDirectory

    override suspend fun doConvertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        executionCount++
        targetPath.parent?.createDirectories()
        writePng(targetPath)
    }

    private fun writePng(path: Path) {
        val image = BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = Color.RED
            graphics.fillRect(0, 0, image.width, image.height)
        } finally {
            graphics.dispose()
        }
        ImageIO.write(image, "png", path.toFile())
    }
}
