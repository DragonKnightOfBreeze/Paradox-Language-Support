package icu.windea.pls.lang.inspections.script.expression

import com.intellij.testFramework.IndexingTestUtil
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
 * @see TooManyExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class TooManyExpressionInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(TooManyExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region basic

    @Test
    fun smoke_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                from = size
                set = { x = 1 y = 1 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun smoke_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Too many key expression `set` (expect at most 1, actual 2)"
            """
            ${weakWarning(m1)}tsst${weakWarningEnd()} = {
                from = size
                set = { x = 1 y = 1 }
                set = { x = 1 y = 1 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun smoke_propertyValueNotSame_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Too many key expression `set` (expect at most 1, actual 2)"
            """
            ${weakWarning(m1)}tsst${weakWarningEnd()} = {
                from = size
                set = { x = 1 y = 1 }
                set = { x = 2 y = 2 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun smoke_propertyValueConfigNotSame_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Too many key expression `set` (expect at most 1, actual 2)"
            """
            ${weakWarning(m1)}tsst${weakWarningEnd()} = {
                from = size
                set = { x = 1 y = 1 }
                set = { width = 1 height = 1 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun smoke_nested_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Too many key expression `y` (expect at most 1, actual 2)"
            """
            test = {
                from = size
                ${weakWarning(m1)}set${weakWarningEnd()} = { x = 1 y = 1 y = 1 }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion

    // region inlined

    @Test
    fun inlined_checkUsages_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test_inline.txt")
        myFixture.configureByText("test_inline.txt") {
            """
                from = size
                set = { x = 1 y = 1 }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                inline_script = test_inline
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun inlined_checkUsages_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test_inline.txt")
        myFixture.configureByText("test_inline.txt") {
            """
                from = size
                set = { x = 1 y = 1 }
                set = { x = 1 y = 1 }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Too many key expression `set` (expect at most 1, actual 2)"
            """
            ${weakWarning(m1)}test${weakWarningEnd()} = {
                inline_script = test_inline
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun inlined_checkDeclarations_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                inline_script = test_inline
            }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test_inline.txt")
        myFixture.configureByText("test_inline.txt") {
            """
                from = size
                set = { x = 1 y = 1 }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun inlined_checkDeclarations_skipForDeclarationRoots_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                inline_script = test_inline
            }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test_inline.txt")
        myFixture.configureByText("test_inline.txt") {
            """
                from = size
                set = { x = 1 y = 1 }
                set = { x = 1 y = 1 }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun inlined_checkDeclarations_nested_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                inline_script = test_inline
            }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test_inline.txt")
        myFixture.configureByText("test_inline.txt") {
            val m1 = "Too many key expression `y` (expect at most 1, actual 2)"
            """
                from = size
                ${weakWarning(m1)}set${weakWarningEnd()} = { x = 1 y = 1 y = 1 }
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
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt", """
            hint = hello_world
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // TODO [test] more tests
}
