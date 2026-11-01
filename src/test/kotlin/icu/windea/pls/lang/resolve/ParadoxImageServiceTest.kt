package icu.windea.pls.lang.resolve

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.util.ImageFrameInfo
import icu.windea.pls.test.ChronicleTestScope
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path

/**
 * @see ParadoxImageService
 * @see ParadoxImageResolveResult
 */
@RunWith(JUnit4::class)
class ParadoxImageServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    @Before
    fun doSetUp() {
        addAdditionalAllowedRoots(Path.of("src", "test", "testData"))
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
        val path = Path.of("build", "tmp", "paradox-image-service-test", "not-image.txt")
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
}
