package icu.windea.pls.lang.inspections.script.expression

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see IncorrectExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectExpressionInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(IncorrectExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureLocalisationFile() {
        markFileInfo(ParadoxGameType.Stellaris, "localisation/00_test_locs.yml")
        myFixture.configureByFile("features/inspections/localisation/00_test_locs.yml")
    }

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
    fun smoke_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/messages/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = "Number out of range (expect matching range: [0..null], actual: -1)"
            """
            start_message = {
                index = ${warning(m1)}-1${warningEnd()}
                tags = { start }
                message_part = { say = hello_world }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion

    // region snippetMatch

    @Test
    fun snippetMatch_definitionSnippet_fullMatch_success() {
        // 定义引用片段的所有模板参数（`test_a`、`b_test`）都能解析为对应类型的定义
        markFileInfo(ParadoxGameType.Stellaris, "common/test_types/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_a = {}
            b_test = {}
            first_type = {
                snippet_def = test
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun snippetMatch_definitionSnippet_partialMatch_failed() {
        // 语义匹配阶段采用宽松策略，但代码检查阶段要求所有模板参数都能解析，
        // 因此缺少 `b_test` 时报告“部分匹配”。
        markFileInfo(ParadoxGameType.Stellaris, "common/test_types/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = ChronicleEpBundle.message("incorrectExpression.definitionSnippet.desc.1", "b_test")
            """
            test_a = {}
            first_type = {
                snippet_def = ${warning(m1)}test${warningEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun snippetMatch_localisationSnippet_fullMatch_success() {
        // 本地化引用片段的所有模板参数（`test_desc`、`test_effect`）都能解析为本地化
        configureLocalisationFile()
        markFileInfo(ParadoxGameType.Stellaris, "common/test_types/test.txt")
        myFixture.configureByText("test.txt") {
            """
            first_type = {
                snippet_loc = test
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun snippetMatch_localisationSnippet_partialMatch_failed() {
        // 语义匹配阶段采用宽松策略，但代码检查阶段要求所有模板参数都能解析，
        // 因此缺少 `partial_effect` 时报告“部分匹配”。
        configureLocalisationFile()
        markFileInfo(ParadoxGameType.Stellaris, "common/test_types/test.txt")
        myFixture.configureByText("test.txt") {
            val m1 = ChronicleEpBundle.message("incorrectExpression.localisationSnippet.desc.1", "partial_effect")
            """
            first_type = {
                snippet_loc = ${warning(m1)}partial${warningEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
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
