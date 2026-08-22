package icu.windea.pls.lang.inspections.script.expression

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see UnresolvedPathReferenceInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnresolvedPathReferenceInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(UnresolvedPathReferenceInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region basic

    @Test
    fun smoke_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/includes/include.txt")
        myFixture.configureByText("include.txt", "# nothing")

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { say = hello_world }
                include = "common/includes/include.txt"
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun incorrectFileExtension_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/includes/include.gfx")
        myFixture.configureByText("include.gfx", "# nothing")

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { say = hello_world }
                include = "common/includes/include.gfx"
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun unresolvedPathReference_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/includes/include.txt")
        myFixture.configureByText("include.txt", "# nothing")

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve file path reference `common/includes/unresolved.txt` (expect matching: filepath)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { say = hello_world }
                include = ${error(m1)}"common/includes/unresolved.txt"${errorEnd()}
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion

    // region ignored

    @Test
    fun noSemantic_ignored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            hint = hello_world
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun outOfDefinitionDeclaration_ignored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt", """
            hint = hello_world
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // TODO [test] more tests
}
