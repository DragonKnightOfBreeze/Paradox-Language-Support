package icu.windea.pls.test.issues

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.inject.injectors.DDSImageReaderCodeInjector
import icu.windea.pls.integrations.images.ImageToolProvider
import icu.windea.pls.integrations.images.ImageToolService
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxModifierIconHintsProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream

/**
 * See: [#396](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/396)
 *
 * 回归测试关注 Issue #396 的关键路径：
 * `DDSImageReaderCodeInjector.read` -> `ImageToolService.read` -> 命令行图片工具。
 *
 * 当图片读取发生在 EDT 或 readAction 中时，`ImageToolService` 必须跳过外部工具，
 * 交回内置 ImageIO 实现，从而避免在内嵌提示计算图标宽度的过程中同步执行命令行并冻结 IDE。
 * 这里使用记录调用的测试 `ImageToolProvider` 模拟外部工具。
 *
 * @see ImageToolService
 * @see DDSImageReaderCodeInjector
 * @see ParadoxModifierIconHintsProvider
 */
@RunWith(JUnit4::class)
class Issue396Test : BasePlatformTestCase() {
    private lateinit var recordingProvider: RecordingImageToolProvider

    @Before
    fun setUpProvider() {
        recordingProvider = RecordingImageToolProvider()
        ExtensionTestUtil.maskExtensions(ImageToolProvider.EP_NAME, listOf(recordingProvider), testRootDisposable)
    }

    @Test
    fun readInEdtSkipsExternalTool_test() {
        val service = ImageToolService.getInstance()
        val stream = createImageInputStream()
        Assert.assertNull(service.read(0, null, stream, "dds", "png"))
        Assert.assertFalse("External image tool must not be used in EDT", recordingProvider.readCalled)
    }

    @Test
    fun readUnderReadActionSkipsExternalTool_test() {
        val result = ApplicationManager.getApplication().executeOnPooledThread<BufferedImage?> {
            ApplicationManager.getApplication().runReadAction<BufferedImage?> {
                ImageToolService.getInstance().read(0, null, createImageInputStream(), "dds", "png")
            }
        }.get()
        Assert.assertNull(result)
        Assert.assertFalse("External image tool must not be used under readAction", recordingProvider.readCalled)
    }

    @Test
    fun convertInEdtSkipsExternalTool_test() {
        val converted = ImageToolService.getInstance().convertImageFormat(
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            "dds",
            "png",
        )
        Assert.assertFalse(converted)
        Assert.assertFalse("External image tool must not be used in EDT", recordingProvider.convertCalled)
    }

    @Test
    fun readOutsideRestrictedContextCallsTool_test() {
        val result = ApplicationManager.getApplication().executeOnPooledThread<BufferedImage?> {
            ImageToolService.getInstance().read(0, null, createImageInputStream(), "dds", "png")
        }.get()
        Assert.assertNotNull(result)
        Assert.assertTrue("External image tool should be used outside EDT/readAction", recordingProvider.readCalled)
    }

    private fun createImageInputStream(): ImageInputStream {
        return ImageIO.createImageInputStream(ByteArrayInputStream(ByteArray(0)))
    }

    private class RecordingImageToolProvider : ImageToolProvider {
        override val name: String = "issue396-recording"

        @Volatile
        var readCalled = false

        @Volatile
        var convertCalled = false

        override fun isAvailable(): Boolean = true

        override fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage {
            readCalled = true
            return BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        }

        override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
            convertCalled = true
        }

        override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
            convertCalled = true
        }
    }
}
