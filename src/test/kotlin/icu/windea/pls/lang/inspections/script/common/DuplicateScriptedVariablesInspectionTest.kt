package icu.windea.pls.lang.inspections.script.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see DuplicateScriptedVariablesInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class DuplicateScriptedVariablesInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(DuplicateScriptedVariablesInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun smokeTest_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            @message = "Hello world"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun smokeTest_differentKeys() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            @message = "Hello world"
            @new_message = "Hello the real world"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun smokeTest_failed() {
        val key = "message"
        val tag = ChronicleBundle.message("inspection.script.duplicateScriptedVariables.desc", key).toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            ${tag.start}@message${tag.end} = "Hello world"
            ${tag.start}@message${tag.end} = "Hello the real world"
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }
}
