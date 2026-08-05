package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxScriptedVariableNameIndex
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableIndexTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Local

    @Test
    fun test_Local() {
        configureMarkedFile("features/index/common/test/local_vars.test.txt")

        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ChronicleIndexKeys.ScriptedVariableName,
            "var",
            project,
            scope,
            ParadoxScriptScriptedVariable::class.java
        )
        Assert.assertTrue(elements.isNotEmpty())
        Assert.assertTrue(elements.any { it.containingFile.virtualFile.name == "local_vars.test.txt" })
    }

    // endregion

    // region Global

    @Test
    fun test_Global() {
        configureMarkedFile("features/index/common/scripted_variables/global_vars.test.txt")

        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ChronicleIndexKeys.ScriptedVariableName,
            "var",
            project,
            scope,
            ParadoxScriptScriptedVariable::class.java
        )
        Assert.assertTrue(elements.any { it.containingFile.virtualFile.name == "global_vars.test.txt" })
    }

    // endregion

    private fun configureMarkedFile(@TestDataFile testDataPath: String, path: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, path)
        return myFixture.configureByFile(testDataPath)
    }
}
