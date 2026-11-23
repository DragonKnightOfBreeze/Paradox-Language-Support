package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
import icu.windea.pls.lang.analysis.ParadoxFileEncodingService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.inspections.script.common.IncorrectFileEncodingInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#456](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/456)
 *
 * In Stellaris 4.6 ("Corona Borealis"), Paradox changed the file encoding convention for script files under `common/name_lists`:
 * - All vanilla name list script files under `common/name_lists` have been converted to UTF-8 without BOM.
 * - If a name list file contains a UTF-8 BOM, reading it causes parsing errors or crashes in game.
 *
 * Cause:
 * - `ParadoxFileEncodingService.useBom()` unconditionally expected BOM for `common/name_lists` in Stellaris.
 * - `ParadoxUtf8BomOptionProvider` unconditionally added BOM when creating new files under `common/name_lists`.
 *
 * Solution:
 * - Check `gameVersion` against `"4.6"`: require BOM only when `gameVersion < 4.6`.
 * - For Stellaris >= 4.6 (or unspecified `gameVersion = null`), require UTF-8 NO BOM.
 *
 * @see icu.windea.pls.lang.analysis.ParadoxFileEncodingService
 * @see icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
 * @see icu.windea.pls.lang.inspections.script.common.IncorrectFileEncodingInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue456Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/456")
        myFixture.enableInspections(IncorrectFileEncodingInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testUseBom_Stellaris46() {
        val rootInfo = createRootInfo(ParadoxGameType.Stellaris, "4.6")
        markFileInfo(rootInfo, "common/name_lists/test.txt")
        val file = myFixture.configureByText("test.txt", "name_list = {}")
        val fileInfo = file.fileInfo
        assertNotNull(fileInfo)
        // Stellaris 4.6+ name_lists should NOT use BOM
        val useBom = ParadoxFileEncodingService.useBom(file, fileInfo!!)
        assertEquals(false, useBom)
    }

    @Test
    fun testUseBom_Stellaris45() {
        val rootInfo = createRootInfo(ParadoxGameType.Stellaris, "4.5")
        markFileInfo(rootInfo, "common/name_lists/test.txt")
        val file = myFixture.configureByText("test.txt", "name_list = {}")
        val fileInfo = file.fileInfo
        assertNotNull(fileInfo)
        // Stellaris < 4.6 name_lists should use BOM
        val useBom = ParadoxFileEncodingService.useBom(file, fileInfo!!)
        assertEquals(true, useBom)
    }

    @Test
    fun testUseBom_StellarisDefaultVersion() {
        val rootInfo = createRootInfo(ParadoxGameType.Stellaris, null)
        markFileInfo(rootInfo, "common/name_lists/test.txt")
        val file = myFixture.configureByText("test.txt", "name_list = {}")
        val fileInfo = file.fileInfo
        assertNotNull(fileInfo)
        // Default to latest (4.6+): should NOT use BOM
        val useBom = ParadoxFileEncodingService.useBom(file, fileInfo!!)
        assertEquals(false, useBom)
    }

    @Test
    fun testUseBom_StellarisOtherScript() {
        val rootInfo = createRootInfo(ParadoxGameType.Stellaris, "4.6")
        markFileInfo(rootInfo, "common/traits/00_traits.txt")
        val file = myFixture.configureByText("00_traits.txt", "trait = {}")
        val fileInfo = file.fileInfo
        assertNotNull(fileInfo)
        val useBom = ParadoxFileEncodingService.useBom(file, fileInfo!!)
        assertEquals(false, useBom)
    }

    @Test
    fun testUtf8BomOptionProvider() {
        val provider = ParadoxUtf8BomOptionProvider()

        val rootInfo46 = createRootInfo(ParadoxGameType.Stellaris, "4.6")
        markFileInfo(rootInfo46, "common/name_lists/test46.txt")
        val file46 = myFixture.configureByText("test46.txt", "name_list = {}")
        assertFalse(provider.shouldAddBOMForNewUtf8File(file46.virtualFile))

        val rootInfo45 = createRootInfo(ParadoxGameType.Stellaris, "4.5")
        markFileInfo(rootInfo45, "common/name_lists/test45.txt")
        val file45 = myFixture.configureByText("test45.txt", "name_list = {}")
        assertTrue(provider.shouldAddBOMForNewUtf8File(file45.virtualFile))

        val rootInfoDefault = createRootInfo(ParadoxGameType.Stellaris, null)
        markFileInfo(rootInfoDefault, "common/name_lists/test_default.txt")
        val fileDefault = myFixture.configureByText("test_default.txt", "name_list = {}")
        assertFalse(provider.shouldAddBOMForNewUtf8File(fileDefault.virtualFile))
    }

    @Test
    fun testInspection_Stellaris46WithoutBom() {
        val rootInfo = createRootInfo(ParadoxGameType.Stellaris, "4.6")
        markFileInfo(rootInfo, "common/name_lists/test.txt")
        myFixture.configureByText("test.txt", "name_list = {}")
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_Stellaris46WithBom() {
        val rootInfo = createRootInfo(ParadoxGameType.Stellaris, "4.6")
        markFileInfo(rootInfo, "common/name_lists/test.txt")
        val file = myFixture.configureByText("test.txt", "name_list = {}")
        VirtualFileBomService.addBom(file.virtualFile, VirtualFileBomService.utf8Bom)
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("BOM") == true })
    }
}
