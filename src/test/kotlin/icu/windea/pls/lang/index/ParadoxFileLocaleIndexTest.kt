package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFileLocaleIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    // region Basic

    @Test
    fun testFileLocaleIndex_English() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/ui/ui_l_english.test.yml")
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        val data = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.FileLocale, myFixture.file.virtualFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_english", key)
    }

    @Test
    fun testFileLocaleIndex_SimpChinese() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/simp_chinese/ui_l_simp_chinese.test.yml")
        myFixture.configureByFile("features/index/localisation/simp_chinese/ui_l_simp_chinese.test.yml")
        val data = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.FileLocale, myFixture.file.virtualFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_simp_chinese", key)
    }

    // endregion
}
