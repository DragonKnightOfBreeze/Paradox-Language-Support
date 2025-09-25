package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxFileLocaleIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testFileLocaleIndex_English() {
        myFixture.configureByFile("features/index/localisation/ui/ui_l_english.test.yml")
        injectFileInfo("localisation/ui/ui_l_english.test.yml", ParadoxGameType.Stellaris)
        val vFile = myFixture.file.virtualFile
        val data = FileBasedIndex.getInstance().getFileData(ParadoxIndexKeys.FileLocale, vFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_english", key)
    }

    fun testFileLocaleIndex_SimpChinese() {
        myFixture.configureByFile("features/index/localisation/simp_chinese/ui_l_simp_chinese.test.yml")
        injectFileInfo("localisation/simp_chinese/ui_l_simp_chinese.test.yml", ParadoxGameType.Stellaris)
        val vFile = myFixture.file.virtualFile
        val data = FileBasedIndex.getInstance().getFileData(ParadoxIndexKeys.FileLocale, vFile, project)
        val key = data.keys.singleOrNull()
        Assert.assertEquals("l_simp_chinese", key)
    }

    private fun injectFileInfo(relPath: String, gameType: ParadoxGameType) {
        val vFile = myFixture.file.virtualFile
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", ParadoxFileType.Localisation, ParadoxRootInfo.Injected(gameType))
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedFileInfo, fileInfo)
        vFile.putUserData(icu.windea.pls.lang.PlsKeys.injectedGameType, gameType)
    }
}
