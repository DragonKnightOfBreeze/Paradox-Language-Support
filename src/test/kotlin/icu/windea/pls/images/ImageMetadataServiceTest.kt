package icu.windea.pls.images

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.test.ChronicleTestScope
import org.junit.Assert
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

    @Test
    fun getMetadata_dds_test() {
        val path = Path.of("src/test/testData/features/relatedItems/gfx/interface/icons/armor/dragon_scale.dds")
        addAdditionalAllowedRoots(path.parent)
        val file = VfsUtil.findFileByIoFile(path.toFile(), true) ?: error("Cannot find file: $path")

        val metadata = ImageMetadataService.getInstance().getMetadata(file, project)

        Assert.assertNotNull(metadata)
        Assert.assertTrue(metadata!!.width > 0)
        Assert.assertTrue(metadata.height > 0)
    }

    @Test
    fun getMetadata_path_test() {
        val path = Path.of("src/test/testData/features/relatedItems/gfx/interface/icons/armor/dragon_scale.dds")

        val metadata = ImageMetadataService.getInstance().getMetadata(path)

        Assert.assertNotNull(metadata)
        Assert.assertTrue(metadata!!.width > 0)
        Assert.assertTrue(metadata.height > 0)
    }
}
