package icu.windea.pls.core.vfs

import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files

/**
 * @see VirtualFileService
 */
@RunWith(JUnit4::class)
class VirtualFileServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region isLightFile

    @Test
    fun isLightFile_basic() {
        // 内存中的虚拟文件属于轻量文件
        val lightFile = LightVirtualFile("test.cwt", "key = value")
        assertTrue(VirtualFileService.isLightFile(lightFile))

        // 通过测试夹具创建的（temp:// 文件系统的）文件不属于轻量文件
        myFixture.configureByText("test.cwt", "key = value")
        assertFalse(VirtualFileService.isLightFile(myFixture.file.virtualFile))
    }

    // endregion

    // region isInjectedFile / isStubFile / isInArchiveFile

    @Test
    fun typeChecks_negative() {
        myFixture.configureByText("test.cwt", "key = value")
        val file = myFixture.file.virtualFile
        // 普通文件不属于注入文件、存根文件或归档文件
        assertFalse(VirtualFileService.isInjectedFile(file))
        assertFalse(VirtualFileService.isStubFile(file))
        assertFalse(VirtualFileService.isInArchiveFile(file))
    }

    // endregion

    // region findDirectory

    @Test
    fun findDirectory_basic() {
        val tempDir = Files.createTempDirectory("virtual-file-service-test")
        try {
            // 不存在的目录且不创建
            val missing = VirtualFileService.findDirectory(tempDir.resolve("missing"), createIfMissing = false)
            assertNull(missing)

            // 不存在的目录且创建
            val created = VirtualFileService.findDirectory(tempDir.resolve("created"), createIfMissing = true)
            assertNotNull(created)
            assertTrue(created!!.isDirectory)

            // 已存在的目录
            val existing = VirtualFileService.findDirectory(tempDir.resolve("created"), createIfMissing = false)
            assertNotNull(existing)
            assertTrue(existing!!.isDirectory)
        } finally {
            FileUtil.delete(tempDir.toFile())
        }
    }

    // endregion
}
