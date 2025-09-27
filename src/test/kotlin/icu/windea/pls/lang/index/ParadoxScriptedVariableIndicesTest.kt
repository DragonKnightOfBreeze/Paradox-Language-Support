package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testScriptedVariableNameIndex_Local() {
        myFixture.configureByFile("script/syntax/_code_settings.test.txt")
        injectFileInfo("common/test/_code_settings.test.txt", ParadoxGameType.Stellaris)
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

    fun testScriptedVariableSearcher_Local() {
        myFixture.configureByFile("script/syntax/_code_settings.test.txt")
        injectFileInfo("common/test/_code_settings.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file.virtualFile).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchLocal("var", selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    fun testScriptedVariableNameIndex_Global() {
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        injectFileInfo("common/scripted_variables/global_vars.test.txt", ParadoxGameType.Stellaris)
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

    fun testScriptedVariableSearcher_Global() {
        myFixture.configureByFile("features/index/common/scripted_variables/global_vars.test.txt")
        injectFileInfo("common/scripted_variables/global_vars.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file).scriptedVariable()
        val results = mutableListOf<String>()
        ParadoxScriptedVariableSearch.searchGlobal("var", selector).processQuery(false) { v ->
            results += v.name ?: ""
            true
        }
        Assert.assertTrue(results.contains("var"))
    }

    fun testScriptedVariableSearcher_Local_SkipAfterCaret() {
        myFixture.configureByFile("features/index/script/local_vars_positions.test.txt")
        injectFileInfo("common/test/local_vars_positions.test.txt", ParadoxGameType.Stellaris)
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

    private fun injectFileInfo(relPath: String, gameType: ParadoxGameType) {
        val vFile = myFixture.file.virtualFile
        val fileType = when {
            relPath.endsWith(".yml", true) -> ParadoxFileType.Localisation
            else -> ParadoxFileType.Script
        }
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", fileType, ParadoxRootInfo.Injected(gameType))
        vFile.putUserData(PlsKeys.injectedFileInfo, fileInfo)
        vFile.putUserData(PlsKeys.injectedGameType, gameType)
    }
}
