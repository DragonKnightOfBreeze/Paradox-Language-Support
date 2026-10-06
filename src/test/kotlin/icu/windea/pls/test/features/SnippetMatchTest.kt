package icu.windea.pls.test.features

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.inspections.script.expression.IncorrectExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.util.ParadoxSnippetManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors as Colors

/**
 * 片段匹配（[DefinitionSnippet][icu.windea.pls.config.CwtDataTypes.DefinitionSnippet] /
 * [LocalisationSnippet][icu.windea.pls.config.CwtDataTypes.LocalisationSnippet]）的回归测试。
 *
 * 使用自行编写的规则文件和脚本文件（均位于 `features/snippet`）：
 * - `test_type` 类型用于验证定义引用片段，要求存在实际的 `test_type` 定义（`test_a`、`b_foo`）。
 * - `localisation` 引用片段用于验证本地化引用片段，要求存在实际的本地化（`test_desc`、`test_effect`）。
 *
 * 覆盖语义匹配、语义高亮（[ParadoxScriptSemanticHighlightingAnnotator][icu.windea.pls.lang.highlighting.ParadoxScriptSemanticHighlightingAnnotator]）、
 * 引用解析、代码补全和代码检查。定义引用片段和本地化引用片段的用例放在各自的分组中，且尽可能对齐。
 *
 * @see icu.windea.pls.config.CwtDataTypes.DefinitionSnippet
 * @see icu.windea.pls.config.CwtDataTypes.LocalisationSnippet
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class SnippetMatchTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/snippet")
        markConfigDirectory("features/snippet/.config")
        initInjectedConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureLocalisationFile() {
        markFileInfo(gameType, "localisation/00_test_locs.yml")
        myFixture.configureByFile("features/snippet/localisation/00_test_locs.yml")
    }

    private fun configureDefinitionSnippetScript(text: String) {
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt", text.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    // region definition snippet

    @Test
    fun definitionSnippet_resolve() {
        configureDefinitionSnippetScript(
            """
            test_a = {}
            b_foo = {}
            first_type = {
                snippet_def = te<caret>st
            }
            """
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            resolved.expectIs<ParadoxDefinitionSnippetLightElement>().name.expectEquals("test")
        }
    }

    @Test
    fun definitionSnippet_completion() {
        configureDefinitionSnippetScript(
            """
            test_a = {}
            b_foo = {}
            first_type = {
                snippet_def = <caret>
            }
            """
        )

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings.orEmpty()
        expectScope {
            lookupElementStrings.contains("test").expectTrue()
            lookupElementStrings.contains("foo").expectTrue()
        }
    }

    @Test
    fun definitionSnippet_notMatched_noReference() {
        configureDefinitionSnippetScript(
            """
            first_type = {
                snippet_def = un<caret>known
            }
            """
        )

        expectScope {
            // 不存在匹配的定义片段，因而不产生引用
            myFixture.findReferenceAtCaret().expectNull()
        }
    }

    @Test
    fun definitionSnippet_semanticAnnotator() {
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt") {
            """
            ${info(Colors.DEFINITION)}test_a${infoEnd()} = {}
            ${info(Colors.DEFINITION)}first_type${infoEnd()} = {
                snippet_def = ${info(Colors.DEFINITION_REFERENCE_SNIPPET)}test${infoEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting(false, true, false)
    }

    @Test
    fun definitionSnippet_unresolvedExpressionInspection() {
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt") {
            val expected = $$"""<test_type>|$_a,b_$"""
            val m = "Cannot resolve value expression `unknown` (expect matching: $expected)"
            """
            first_type = {
                snippet_def = ${error(m)}unknown${errorEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun definitionSnippet_incorrectExpressionInspection() {
        myFixture.enableInspections(IncorrectExpressionInspection::class.java)
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt") {
            val m = ChronicleEpBundle.message("incorrectExpression.definitionSnippet.desc.1", "b_test")
            """
            test_a = {}
            b_foo = {}
            first_type = {
                snippet_def = ${warning(m)}test${warningEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun definitionSnippet_relatedDefinitions() {
        configureDefinitionSnippetScript(
            """
            test_a = {}
            first_type = {
                snippet_def = te<caret>st
            }
            """
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            val snippet = resolved.expectIs<ParadoxDefinitionSnippetLightElement>()
            val relatedNames = ParadoxSnippetManager.getRelatedDefinitions(snippet).map { it.definitionInfo?.name }
            relatedNames.expectUnorderedEquals("test_a")
        }
    }

    // endregion

    // region localisation snippet

    @Test
    fun localisationSnippet_resolve() {
        configureLocalisationFile()
        configureDefinitionSnippetScript(
            """
            first_type = {
                snippet_loc = te<caret>st
            }
            """
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            resolved.expectIs<ParadoxLocalisationSnippetLightElement>().name.expectEquals("test")
        }
    }

    @Test
    fun localisationSnippet_completion() {
        configureLocalisationFile()
        configureDefinitionSnippetScript(
            """
            first_type = {
                snippet_loc = <caret>
            }
            """
        )

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings.orEmpty()
        expectScope {
            lookupElementStrings.contains("test").expectTrue()
        }
    }

    @Test
    fun localisationSnippet_notMatched_noReference() {
        configureLocalisationFile()
        configureDefinitionSnippetScript(
            """
            first_type = {
                snippet_loc = un<caret>known
            }
            """
        )

        expectScope {
            // 不存在匹配的本地化引用片段，因而不产生引用
            myFixture.findReferenceAtCaret().expectNull()
        }
    }

    @Test
    fun localisationSnippet_semanticAnnotator() {
        configureLocalisationFile()
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt") {
            """
            ${info(Colors.DEFINITION)}first_type${infoEnd()} = {
                snippet_loc = ${info(Colors.LOCALISATION_REFERENCE_SNIPPET)}test${infoEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting(false, true, false)
    }

    @Test
    fun localisationSnippet_unresolvedExpressionInspection() {
        configureLocalisationFile()
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt") {
            val expected = $$"localisation|$_desc,$_effect"
            val m = "Cannot resolve value expression `unknown` (expect matching: $expected)"
            """
            first_type = {
                snippet_loc = ${error(m)}unknown${errorEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun localisationSnippet_incorrectExpressionInspection() {
        configureLocalisationFile()
        myFixture.enableInspections(IncorrectExpressionInspection::class.java)
        markFileInfo(gameType, "common/test_types/00_test_types.txt")
        myFixture.configureByText("00_test_types.txt") {
            val m = ChronicleEpBundle.message("incorrectExpression.localisationSnippet.desc.1", "partial_effect")
            """
            first_type = {
                snippet_loc = ${warning(m)}partial${warningEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun localisationSnippet_relatedLocalisations() {
        configureLocalisationFile()
        configureDefinitionSnippetScript(
            """
            first_type = {
                snippet_loc = te<caret>st
            }
            """
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            val snippet = resolved.expectIs<ParadoxLocalisationSnippetLightElement>()
            val relatedNames = ParadoxSnippetManager.getRelatedLocalisations(snippet).map { it.name }
            relatedNames.expectUnorderedEquals("test_desc", "test_effect")
        }
    }

    // endregion
}
