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
 * @see UnresolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnresolvedExpressionInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
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

    @Test
    fun semantic_smoke_unresolvedTopProperty_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `say` (expect matching: index, tags, message_part, include)"
            """
            start_message = {
                ${error(m1)}say${errorEnd()} = hello
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unresolvedTopPropertyValue__failed() {
        // TODO 3.0.2
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = dynamic
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unresolvedLeafProperty_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { ${error(m1)}send${errorEnd()} = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unresolvedLeafDirectValue_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve value expression `delay` (expect matching: hidden)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_part = { ${error(m1)}delay${errorEnd()} }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unmatchedParameterizedTopProperty_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                info_$p = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_parameterizedTopProperty_success() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                message_$p = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_success() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                $p = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unmatchedParameterizedTopProperty_ignoreUnresolvedChild_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                info_$p = { send = how_to }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_parameterizedTopProperty_withUnresolvedChild_asParameterizedMatch_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                message_$p = { send = how_to }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_withUnresolvedChild_asAnyDataType_success() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                $p = { send = how_to }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion

    // TODO [test] more tests
}
