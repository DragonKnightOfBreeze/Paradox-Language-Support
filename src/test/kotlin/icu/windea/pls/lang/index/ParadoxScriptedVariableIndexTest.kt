package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
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
class ParadoxScriptedVariableIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testScriptedVariableNameIndex_Local() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/local_vars.test.txt")
        myFixture.configureByFile("features/index/local_vars.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            PlsIndexKeys.ScriptedVariableName,
            "var",
            project,
            scope,
            ParadoxScriptScriptedVariable::class.java
        )
        Assert.assertTrue(elements.isNotEmpty())
        Assert.assertTrue(elements.any { it.containingFile.virtualFile.name == "local_vars.test.txt" })
    }

    @Test
    fun testScriptedVariableSearcher_Local() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/local_vars.test.txt")
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
        markFileInfo(ParadoxGameType.Stellaris, "common/test/local_vars.test.txt")
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
        markFileInfo(ParadoxGameType.Stellaris, "common/test/local_vars.test.txt")
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

    @Test
    fun testScriptedVariableNameIndex_Global() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_variables/global_vars.test.txt")
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            PlsIndexKeys.ScriptedVariableName,
            "var",
            project,
            scope,
            ParadoxScriptScriptedVariable::class.java
        )
        Assert.assertTrue(elements.any { it.containingFile.virtualFile.name == "global_vars.test.txt" })
    }

    @Test
    fun testScriptedVariableSearcher_Global() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_variables/global_vars.test.txt")
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
}
