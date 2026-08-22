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
 * @see ConflictingExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ConflictingExpressionInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(ConflictingExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region basic

    @Test
    fun smoke_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion

    // region blockMatch

    @Test
    fun blockMatch_empty_vs_empty_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_dimension = {
                empty = {}
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun blockMatch_empty_vs_orEmpty_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_dimension = {
                set_or_empty = {}
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun blockMatch_empty_vs_notEmpty_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_dimension = {
                set = {}
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun blockMatch_lenient_vs_notEmpty_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_dimension = {
                set = { x = 1 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun blockMatch_exact_vs_notEmpty_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_dimension = {
                set = { x = 1 y = 1 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun blockMatch_conflicting_vs_notEmpty_TODO() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_dimension = {
                set = { x = 1 height = 1 }
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
