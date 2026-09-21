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
 * @see MissingExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class MissingExpressionInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(MissingExpressionInspection::class.java)
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
            val m1 = "Missing key expression `set` (expect at least 1, actual 0)"
            """
            ${error(m1)}test${errorEnd()} = {
                from = size
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun smoke_nested_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Missing key expression `y` (expect at least 1, actual 0)"
            """
            test = {
                from = size
                ${error(m1)}set${errorEnd()} = { x = 1 }
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
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/coordinates/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Missing key expression `set` (expect at least 1, actual 0)"
            """
            ${error(m1)}test${errorEnd()} = {
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
            val m1 = "Missing key expression `y` (expect at least 1, actual 0)"
            """
                from = size
                ${error(m1)}set${errorEnd()} = { x = 1 }
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
