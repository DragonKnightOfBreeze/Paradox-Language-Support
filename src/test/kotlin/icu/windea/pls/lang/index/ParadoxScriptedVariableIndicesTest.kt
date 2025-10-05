package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testScriptedVariableNameIndex_Local() {
        myFixture.configureByFile("features/index/script/local_vars.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/test/local_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.ScriptedVariableName,
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
        myFixture.configureByFile("features/index/script/local_vars.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/test/local_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file.virtualFile).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal("var", selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    @Test
    fun testScriptedVariableSearcher_Local_SkipAfterCaret() {
        myFixture.configureByFile("features/index/script/local_vars.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/test/local_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val context = myFixture.file.findElementAt(myFixture.caretOffset)!!
        val selector = selector(project, context).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal(null, selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue("Expected to contain variable defined before caret", results.contains("var"))
        Assert.assertFalse("Should skip variable defined after caret", results.contains("other_var"))
    }

    @Test
    fun testScriptedVariableNameIndex_Global() {
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/scripted_variables/global_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.ScriptedVariableName,
            "var",
            project,
            scope,
            ParadoxScriptScriptedVariable::class.java
        )
        Assert.assertTrue(elements.any { it.containingFile.virtualFile.name == "global_vars.test.txt" })
    }

    @Test
    fun testScriptedVariableSearcher_Global() {
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/scripted_variables/global_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchGlobal("var", selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }
}
