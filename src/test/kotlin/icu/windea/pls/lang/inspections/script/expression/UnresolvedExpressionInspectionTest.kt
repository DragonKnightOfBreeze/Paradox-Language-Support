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

    @Test
    fun unresolvedTopPropertyKey_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `say` (expect matching: index, tags, message_part, include, notes)"
            """
            start_message = {
                ${error(m1)}say${errorEnd()} = hello
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun unresolvedTopPropertyValue__failed() {
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
    fun unresolvedLeafPropertyKey_failed() {
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
    fun unresolvedLeafPropertyValue_failed() {
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
    fun unresolvedLeafDirectValue_failed() {
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

    // region parameterized

    @Test
    fun mismatchedParameterizedTopProperty_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include, notes)"
            """
            start_message = {
                ${error(m1)}info_$p${errorEnd()} = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun mismatchedParameterizedTopProperty_unresolvedLeafPropertyKey_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include, notes)"
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
    fun mismatchedParameterizedTopProperty_unresolvedLeafDirectValue_failed() {
        val p = "\$param$"

        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `info_\$param\$` (expect matching: index, tags, message_part, include, notes)"
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
    fun parameterizedTopProperty_success() {
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
    fun parameterizedTopProperty_unresolvedLeafPropertyKey_failed() {
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
    fun parameterizedTopProperty_unresolvedLeafPropertyValue_failed() {
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
    fun parameterizedTopProperty_unresolvedLeafDirectValue_failed() {
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
    fun parameterizedTopProperty_withInferredType_noEffect_success() {
        val p = "\$part$"

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
    fun fullParameterizedTopProperty_success() {
        val p = "\$param$"

        // $param$ -> message_part (matched)
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
    fun fullParameterizedTopProperty_unresolvedLeafPropertyKey_failed() {
        val p = "\$param$"

        // $param$ -> message_part (mismatched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say, note)"
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
    fun fullParameterizedTopProperty_unresolvedLeafPropertyValue_failed() {
        val p = "\$param$"

        // $param$ -> message_part (mismatched)
        // contextConfigs = [hint, say] (no `note`)
        // expectedConfigs = [hint, say, note] (flatten and collect context configs from parent context configs)
        // this is correct and designed so atm for full parameterized property keys
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say, note)"
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
    fun fullParameterizedTopProperty_lenientMatchedLeafDirectValue_success() {
        val p = "\$param$"

        // $param$ -> tags (matched)
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
    fun fullParameterizedTopProperty_unresolvedLeafDirectValue_failed() {
        val p = "\$param$"

        // $param$ -> ? (mismatched)
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
    fun fullParameterizedTopProperty_withInferredType_mismatchedLeafPropertyKey_failed() {
        val p = "\$message_part$"

        // $param$ -> message_part (mismatched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `send` (expect matching: hint, say, note)"
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
    fun fullParameterizedTopProperty_withInferredType_exactMatchedLeafDirectValue_success() {
        val p = "\$message_part$"

        // $param$ -> message_part (matched)
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

    @Test
    fun fullParameterizedTopProperty_withInferredType_lenientMatchedLeafDirectValue_failed() {
        val p = "\$message_part$"

        // $param$ -> message_part (mismatched)
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve value expression `delay` (expect matching: hidden)"
            """
            start_message = {
                index = 0
                tags = { start }
                $p = { ${error(m1)}delay${errorEnd()} }
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
                set_empty = {}
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
    fun blockMatch_conflicting_vs_notEmpty_success() {
        // checked by `ConflictingExpressionInspection`
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

    @Test
    fun blockMatch_mismatched_vs_notEmpty_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/dimensions/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Cannot resolve key expression `z` (expect matching: x, y, width, height)"
            """
            test_dimension = {
                set = { ${error(m1)}z${errorEnd()} = 1 }
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

    @Test
    fun smoke_incorrectFileExtension_ignored() {
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
    fun smoke_unresolvedPathReference_ignored() {
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

    // endregion

    // TODO [test] more tests
}
