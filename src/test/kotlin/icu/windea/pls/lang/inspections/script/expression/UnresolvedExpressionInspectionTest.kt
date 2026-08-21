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
    fun semantic_smoke_unresolvedTopPropertyKey_failed() {
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
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve value expression `dynamic` (expect matching: int[0..inf])"
            """
            start_message = {
                index = ${error(m1)}dynamic${errorEnd()}
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_unresolvedLeafPropertyKey_failed() {
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
    fun semantic_smoke_unresolvedLeafPropertyValue_failed() {
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

    // endregion

    // region semantic parameterized

    @Test
    fun semantic_smoke_mismatchedParameterizedTopProperty_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include)"
            """
            start_message = {
                ${error(m1)}info_$p${errorEnd()} = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_mismatchedParameterizedTopProperty_unresolvedLeafPropertyKey_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include)"
            """
            start_message = {
                index = 0
                tags = { start }
                ${error(m1)}info_$p${errorEnd()} = { send = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_mismatchedParameterizedTopProperty_unresolvedLeafPropertyValue_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include)"
            """
            start_message = {
                index = 0
                tags = { start }
                ${error(m1)}info_$p${errorEnd()} = { send = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_mismatchedParameterizedTopProperty_unresolvedLeafDirectValue_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include)"
            """
            start_message = {
                index = 0
                tags = { start }
                ${error(m1)}info_$p${errorEnd()} = { delay }
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
    fun semantic_smoke_parameterizedTopProperty_unresolvedLeafPropertyKey_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_$p = { ${error(m1)}send${errorEnd()} = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_parameterizedTopProperty_unresolvedLeafPropertyValue_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_$p = { ${error(m1)}send${errorEnd()} = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_parameterizedTopProperty_unresolvedLeafDirectValue_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve value expression `delay` (expect matching: hidden)"
            """
            start_message = {
                index = 0
                tags = { start }
                message_$p = { ${error(m1)}delay${errorEnd()} }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_success() {
        val p = "\$param$"

        // $param$ -> message_part -> (matched)
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
    fun semantic_smoke_fullParameterizedTopProperty_unresolvedLeafPropertyKey_failed() {
        val p = "\$param$"

        // $param$ -> message_part -> (mismatched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say)"
            """
            start_message = {
                index = 0
                tags = { start }
                $p = { ${error(m1)}send${errorEnd()} = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_unresolvedLeafPropertyValue_failed() {
        val p = "\$param$"

        // $param$ -> message_part -> (mismatched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say)"
            """
            start_message = {
                index = 0
                tags = { start }
                $p = { ${error(m1)}send${errorEnd()} = farewell }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_lenientMatchedLeafDirectValue_success() {
        val p = "\$param$"

        // $param$ -> tags -> (matched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = 0
                tags = { start }
                $p = { delay }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_unresolvedLeafDirectValue_failed() {
        val p = "\$param$"

        // $param$ -> ? -> (mismatched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve value expression `{...}` (expect matching: hidden, value[message_tag])"
            """
            start_message = {
                index = 0
                tags = { start }
                $p = { ${error(m1)}{}${errorEnd()} }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun semantic_smoke_fullParameterizedTopProperty_withInferredType_exactMatchedLeafDirectValue_success() {
        val p = "\$message_part$"

        // $param$ -> message_part -> (matched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            """
            start_message = {
                index = 0
                tags = { start }
                $p = { hidden }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // @Test
    // fun semantic_smoke_fullParameterizedTopProperty_withInferredType_lenientMatchedLeafDirectValue_failed() {
    //     val p = "\$message_part$"
    //
    //     // $param$ -> message_part -> (mismatched)
    //     markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
    //     myFixture.configureByText("test.txt") {
    //         val m1 = "Cannot resolve value expression `hidden` (expect matching: hidden)"
    //         """
    //         start_message = {
    //             index = 0
    //             tags = { start }
    //             $p = { ${error(m1)}delay${errorEnd()} }
    //         }
    //         """.trimIndent()
    //     }
    //     myFixture.checkHighlighting()
    // }

    // endregion

    // TODO [test] more tests
}
