package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.selector
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
class ParadoxScriptedVariableSearcherTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    // region Local

    @Test
    fun testScriptedVariableSearcher_Local() {
        markFileInfo(gameType, "common/test/local_vars.test.txt")
        myFixture.configureByFile("features/index/local_vars.test.txt")
        val project = project
        val selector = selector(project, myFixture.file.virtualFile).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal("var", selector).process { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    @Test
    fun testScriptedVariableSearcher_Local_SkipAfterCaret() {
        markFileInfo(gameType, "common/test/local_vars.test.txt")
        myFixture.configureByFile("features/index/local_vars.test.txt")
        val project = project
        val selector = selector(project, myFixture.file.findElementAt(myFixture.caretOffset)!!).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal(null, selector).process { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue("Expected to contain variable defined before caret", results.contains("var"))
        Assert.assertFalse("Should skip variable defined after caret", results.contains("other_var"))
    }

    @Test
    fun testScriptedVariableSearcher_Local_WithOverride() {
        markFileInfo(gameType, "common/test/local_vars.test.txt")
        myFixture.configureByFile("features/index/local_vars.test.txt")
        val project = project
        run {
            val selector = selector(project, myFixture.file.findElementAt(myFixture.caretOffset)!!).scriptedVariable()
            val result = ParadoxScriptedVariableSearch.searchLocal(null, selector).findFirst()
            Assert.assertEquals("0", result?.value)
        }
        // NOTE 无法通过 - 使用 `find()` 进行查询时，不会在同文件中适用后续覆盖（这是在解析引用级别处理的）
        // run {
        //     val selector = selector(project, myFixture.file.findElementAt(myFixture.caretOffset)!!).scriptedVariable()
        //     val result = ParadoxScriptedVariableSearch.searchLocal(null, selector).find()
        //     Assert.assertEquals("1", result?.value)
        // }
        run {
            val selector = selector(project, myFixture.file.findElementAt(myFixture.caretOffset)!!).scriptedVariable()
            val result = ParadoxScriptedVariableSearch.searchLocal(null, selector).findAll().lastOrNull()
            Assert.assertEquals("1", result?.value)
        }
    }

    // endregion

    // region Global

    @Test
    fun testScriptedVariableSearcher_Global() {
        markFileInfo(gameType, "common/scripted_variables/global_vars.test.txt")
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        val project = project
        val selector = selector(project, myFixture.file).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchGlobal("var", selector).process { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    // endregion
}
