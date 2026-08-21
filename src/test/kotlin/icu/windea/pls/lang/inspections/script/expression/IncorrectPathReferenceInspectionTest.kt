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
 * @see IncorrectPathReferenceInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectPathReferenceInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(IncorrectPathReferenceInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region noSmantic

    @Test
    fun noSemantic_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            hint = hello_world
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region semantic

    @Test
    fun outOfDefinitionDeclaration_ignored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt", """
            hint = hello_world
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_success() {
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
    fun semantic_smoke_incorrectFileExtension_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/includes/include.gfx")
        myFixture.configureByText("include.gfx", "# nothing")

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "File extension of path reference `common/includes/include.gfx` is incorrect (expect: txt)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { say = hello_world }
                include = ${warning(m1)}"common/includes/include.gfx"${warningEnd()}
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unresolvedPathReference_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/includes/include.txt")
        myFixture.configureByText("include.txt", "# nothing")

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { say = hello_world }
                include = "common/includes/unresolved.txt"
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // TODO [test] more tests
}
