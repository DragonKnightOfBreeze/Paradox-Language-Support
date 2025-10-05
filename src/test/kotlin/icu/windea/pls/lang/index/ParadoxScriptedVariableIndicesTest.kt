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
    fun scriptedVariableNameIndex_Local() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("script/syntax/_code_settings.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/_code_settings.test.txt", ParadoxGameType.Stellaris)
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
        Assert.assertTrue(elements.any { it.containingFile.virtualFile.name == "_code_settings.test.txt" })
    }

    @Test
    fun scriptedVariableSearcher_Local() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("script/syntax/_code_settings.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/_code_settings.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal("var", selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    @Test
    fun scriptedVariableNameIndex_Global() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/scripted_variables/global_vars.test.txt", ParadoxGameType.Stellaris)
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
    fun scriptedVariableSearcher_Global() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/scripted_variables/global_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchGlobal("var", selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    @Test
    fun scriptedVariableSearcher_Local_SkipAfterCaret() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/script/local_vars_positions.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/local_vars_positions.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val context = myFixture.file.findElementAt(myFixture.caretOffset)!!
        val selector = selector(project, context).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal(null, selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue("Expected to contain variable defined before caret", results.contains("a"))
        Assert.assertFalse("Should skip variable defined after caret", results.contains("b"))
    }
}
