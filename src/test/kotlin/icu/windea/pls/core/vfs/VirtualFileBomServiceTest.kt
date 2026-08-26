package icu.windea.pls.core.vfs

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see VirtualFileBomService
 */
@RunWith(JUnit4::class)
class VirtualFileBomServiceTest : BasePlatformTestCase() {
    // region hasBom

    @Test
    fun hasBom_basic() {
        val file = myFixture.tempDirFixture.createFile("test.txt", "hello")
        // 初始没有 BOM
        assertTrue(VirtualFileBomService.hasBom(file, null))
        assertFalse(VirtualFileBomService.hasBom(file, VirtualFileBomService.utf8Bom))
    }

    // endregion

    // region addBom / removeBom

    @Test
    fun addBomAndRemoveBom_basic() {
        val file = myFixture.tempDirFixture.createFile("test.txt", "hello")
        val bom = VirtualFileBomService.utf8Bom
        val original = "hello".toByteArray()

        // 添加 BOM
        VirtualFileBomService.addBom(file, bom)
        assertTrue(VirtualFileBomService.hasBom(file, bom))
        assertFalse(VirtualFileBomService.hasBom(file, null))
        assertEquals(original.size + bom.size, file.contentsToByteArray().size)

        // 移除 BOM
        VirtualFileBomService.removeBom(file, bom)
        assertTrue(VirtualFileBomService.hasBom(file, null))
        assertFalse(VirtualFileBomService.hasBom(file, bom))
        assertTrue(file.contentsToByteArray().contentEquals(original))
    }

    // endregion
}
