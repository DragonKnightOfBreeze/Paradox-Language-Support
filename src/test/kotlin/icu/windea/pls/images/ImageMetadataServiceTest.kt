package icu.windea.pls.images

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.util.io.toPath
import icu.windea.pls.test.ChronicleTestScope
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path

/**
 * @see ImageMetadataService
 */
@RunWith(JUnit4::class)
class ImageMetadataServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        // 注册内置的 DDS/TGA ImageIO SPI，确保测试不依赖插件启动生命周期
        ImageContext.init()
    }

    @Test
    fun getMetadata_byFile_png_test() {
        val path = "src/test/testData/images/mod_unknown.png".toPath()
        assert_getMetadata_byFile(path)
    }

    @Test
    fun getMetadata_byFile_dds_test() {
        val path = "src/test/testData/images/mod_unknown.dds".toPath()
        assert_getMetadata_byFile(path)
    }

    @Test
    fun getMetadata_byFile_tga_test() {
        val path = "src/test/testData/images/mod_unknown.tga".toPath()
        assert_getMetadata_byFile(path)
    }

    @Test
    fun getMetadata_byPath_png_test() {
        val path = "src/test/testData/images/mod_unknown.png".toPath()
        assert_getMetadata_byPath(path)
    }

    @Test
    fun getMetadata_byPath_dds_test() {
        val path = "src/test/testData/images/mod_unknown.dds".toPath()
        assert_getMetadata_byPath(path)
    }

    @Test
    fun getMetadata_byPath_tga_test() {
        val path = "src/test/testData/images/mod_unknown.tga".toPath()
        assert_getMetadata_byPath(path)
    }

    @Test
    fun getMetadataOrNull_missingPath_test() {
        val path = Path.of("build", "tmp", "image-metadata-missing.png")
        Assert.assertNull(ImageMetadataService.getInstance().getMetadataOrNull(path))
    }

    @Test
    fun getMetadata_missingPath_test() {
        val path = Path.of("build", "tmp", "image-metadata-missing.png")
        Assert.assertThrows(IllegalStateException::class.java) {
            ImageMetadataService.getInstance().getMetadata(path)
        }
    }

    @Test
    fun getMetadataOrNull_invalidImage_test() {
        val path = Path.of("build", "tmp", "image-metadata-invalid.png")
        path.toFile().parentFile.mkdirs()
        path.toFile().writeText("not an image")
        Assert.assertNull(ImageMetadataService.getInstance().getMetadataOrNull(path))
    }

    // region Helpers

    private fun assert_getMetadata_byFile(path: Path) {
        addAdditionalAllowedRoots(path.parent)
        val file = VfsUtil.findFileByIoFile(path.toFile(), true) ?: error("Cannot find file: $path")

        val metadata = ImageMetadataService.getInstance().getMetadata(file, project)
        Assert.assertTrue(metadata.width > 0)
        Assert.assertTrue(metadata.height > 0)
    }

    private fun assert_getMetadata_byPath(path: Path) {
        val metadata = ImageMetadataService.getInstance().getMetadata(path)
        Assert.assertTrue(metadata.width > 0)
        Assert.assertTrue(metadata.height > 0)
    }

    // endregion
}
