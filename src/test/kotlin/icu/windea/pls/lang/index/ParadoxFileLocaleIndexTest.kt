package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFileLocaleIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testFileLocaleIndex_English() {
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val data = FileBasedIndex.getInstance().getFileData(ParadoxIndexKeys.FileLocale, myFixture.file.virtualFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_english", key)
    }

    @Test
    fun testFileLocaleIndex_SimpChinese() {
        myFixture.configureByFile("features/index/localisation/simp_chinese/ui_l_simp_chinese.test.yml")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "localisation/simp_chinese/ui_l_simp_chinese.test.yml", ParadoxGameType.Stellaris)
        val data = FileBasedIndex.getInstance().getFileData(ParadoxIndexKeys.FileLocale, myFixture.file.virtualFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_simp_chinese", key)
    }
}
