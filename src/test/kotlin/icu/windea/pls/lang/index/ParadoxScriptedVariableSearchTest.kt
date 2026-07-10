package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
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

/**
 * @see ParadoxScriptedVariableSearch
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableSearchTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun markAndConfigureByFile(@TestDataFile testDataPath: String, relPath: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, relPath)
        return myFixture.configureByFile(testDataPath)
    }

    // region Local

    @Test
    fun test_Local() {
        markAndConfigureByFile("features/index/common/test/local_vars.test.txt")

        val selector = ParadoxScriptedVariableSearch.selector(project, myFixture.file.virtualFile)
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal("var", selector).process { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    @Test
    fun test_Local_SkipAfterCaret() {
        markAndConfigureByFile("features/index/common/test/local_vars.test.txt")

        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!
        val selector = ParadoxScriptedVariableSearch.selector(project, element)
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal(null, selector).process { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue("Expected to contain variable defined before caret", results.contains("var"))
        Assert.assertFalse("Should skip variable defined after caret", results.contains("other_var"))
    }

    @Test
    fun test_Local_withOverride() {
        markAndConfigureByFile("features/index/common/test/local_vars.test.txt")

        run {
            val element = myFixture.file.findElementAt(myFixture.caretOffset)!!
            val selector = ParadoxScriptedVariableSearch.selector(project, element)
            val result = ParadoxScriptedVariableSearch.searchLocal(null, selector).findFirst()
            Assert.assertEquals("0", result?.value)
        }
        // NOTE 无法通过 - 使用 `find()` 进行查询时，不会在同文件中适用后续覆盖（这是在解析引用级别处理的）
        // run {
        //     val element = myFixture.file.findElementAt(myFixture.caretOffset)!!
        //     val selector = ParadoxScriptedVariableSearch.selector(project, element)
        //     val result = ParadoxScriptedVariableSearch.searchLocal(null, selector).find()
        //     Assert.assertEquals("1", result?.value)
        // }
        run {
            val element = myFixture.file.findElementAt(myFixture.caretOffset)!!
            val selector = ParadoxScriptedVariableSearch.selector(project, element)
            val result = ParadoxScriptedVariableSearch.searchLocal(null, selector).findAll().lastOrNull()
            Assert.assertEquals("1", result?.value)
        }
    }

    // endregion

    // region Global

    @Test
    fun test_Global() {
        markAndConfigureByFile("features/index/common/scripted_variables/global_vars.test.txt")

        val selector = ParadoxScriptedVariableSearch.selector(project, myFixture.file)
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchGlobal("var", selector).process { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    // endregion
}
