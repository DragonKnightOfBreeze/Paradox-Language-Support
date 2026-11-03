package icu.windea.pls.test.issues

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.manipulation.CwtConfigInlineService
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#430](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/430)
 *
 * Geographic region short keys (e.g., `greek_heartlands`) and their `any_*_in_<geographic_region>`
 * iterators were broken: the short keys were no longer resolved, and the iterators were neither
 * recognized nor completed.
 *
 * Cause:
 * - When resolving `ParadoxTemplateExpression` in `incomplete` mode, parsing was aborted directly when
 *   the matched reference fragment text was empty (the definition name is not typed yet). An empty
 *   fragment should not abort parsing in `incomplete` mode.
 * - When collecting the configs applicable to code completion, the parent config was not inlined before
 *   reading its child configs, so the inlined child configs were missing.
 * - For code completion in template expressions, in addition to providing it for reference snippets (e.g., definition references),
 *   it is also necessary to provide it for constant snippets which was not done previously.
 *   This requires improving the matching and resolution logic in incomplete-mode.
 *
 * @see icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
 * @see icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
 * @see icu.windea.pls.config.match.CwtTemplateMatchService.match
 * @see icu.windea.pls.lang.match.ParadoxTemplateMatchService.matches
 * @see icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
 * @see icu.windea.pls.config.manipulation.CwtConfigInlineService.inlineForConfig
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue430Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/430")
        initConfigGroups(project, ParadoxGameType.Vic3)
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region basic cases

    @Test
    fun testInspection_GeographicRegionShortKeyAndIterator() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByFile("issues/430/common/journal_entries/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_GeographicRegionIteratorForState() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_state_in_greek_heartlands = {
                        NOT = { owner = root }
                    }
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testCompletion_GeographicRegionIteratorForState() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_state_in_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "greek_heartlands", "anatolia")
    }

    @Test
    fun testCompletion_GeographicRegionIterator() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "greek_heartlands", "anatolia")
    }

    // endregion

    // region extended cases

    @Test
    fun testConfig_AliasWithTemplateSubNameAndSingleAliasRight() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val alias = configGroup.aliasGroups["trigger"]?.get("any_state_in_<geographic_region_short_key>")?.firstOrNull()
        expectScope {
            alias.expectNotNull()
            alias.subNameExpression.type.expectEquals(CwtDataTypes.Template)
            val inlined = CwtConfigInlineService.inlineAlias(alias.config, alias)
            inlined.expectNotNull()
            // 作为属性键的模板表达式应被正确保留（不应是 `alias[...]`）
            inlined.keyExpression.type.expectEquals(CwtDataTypes.Template)
            inlined.keyExpression.expressionString.expectEquals("any_state_in_<geographic_region_short_key>")
            // 单别名应被内联为对应的块规则
            inlined.valueExpression.type.expectEquals(CwtDataTypes.Block)
        }
    }

    @Test
    fun testConfig_AliasTemplateShouldOutrankGenericDefinitionAlias() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val genericAlias = configGroup.aliasGroups["trigger"]?.get("<script_value>")?.firstOrNull()
        val specificAlias = configGroup.aliasGroups["trigger"]?.get("any_state_in_<geographic_region_short_key>")?.firstOrNull()
        expectScope {
            genericAlias.expectNotNull()
            specificAlias.expectNotNull()
            val genericConfig = CwtConfigInlineService.inlineAlias(genericAlias.config, genericAlias)
            val specificConfig = CwtConfigInlineService.inlineAlias(specificAlias.config, specificAlias)
            genericConfig.expectNotNull()
            specificConfig.expectNotNull()
            // 模板表达式（作为属性键）比纯粹的定义引用更精确，应当被优先选择
            val selected = CwtConfigManager.selectSinglePrioritizedConfig(listOf(genericConfig, specificConfig))
            selected.expectNotNull()
            selected.keyExpression.type.expectEquals(CwtDataTypes.Template)
            selected.keyExpression.expressionString.expectEquals("any_state_in_<geographic_region_short_key>")
        }
    }

    @Test
    fun testConfig_ForShortKey() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val typeName = "geographic_region_short_key"
        val typeConfig = configGroup.types[typeName]
        expectScope {
            typeConfig.expectNotNull()
            typeConfig.skipRootKey.expectEquals(listOf(listOf("any")))
            typeConfig.nameField.expectEquals("-")
        }
        val declarationConfig = configGroup.declarations[typeName]
        expectScope {
            declarationConfig.expectNotNull() // should be explicitly declared
            declarationConfig.config.value.expectEquals("scalar") // should be `scalar` (not `<geographic_region_short_key>`)
        }
    }

    @Test
    fun testInspection_ForShortKey() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            geographic_region_city_serenity = {
                short_key = "city_serenity"
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting() // should be no problems since `geographic_region_short_key = scalar` is explicitly added
    }

    @Test
    fun testCompletion_ForTemplateEmpty() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    <caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "any_country_in_")
    }

    @Test
    fun testCompletion_ForTemplateWithPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "any_country_in_")
    }

    @Test
    fun testCompletion_ForTemplateWithLongerPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        // NOTE 该输入只有唯一一个候选项（`any_country_in_`），若不禁用自动补全，
        //  `myFixture.complete` 会直接插入候选项并修改 PSI，导致意外报错：PSI and index do not match.
        //  参见 `Issue374Test.testCompletion_DynamicValue`。
        val settings = CodeInsightSettings.getInstance()
        val autoComplete = settings.AUTOCOMPLETE_ON_CODE_COMPLETION
        settings.AUTOCOMPLETE_ON_CODE_COMPLETION = false
        try {
            myFixture.complete(CompletionType.BASIC)
            assertContainsElements(myFixture.lookupElementStrings!!, "any_country_in_")
        } finally {
            settings.AUTOCOMPLETE_ON_CODE_COMPLETION = autoComplete
        }
    }

    @Test
    fun testCompletion_ForReferenceSnippetEmpty() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSize(3, lookupElementStrings)
        assertContainsElements(lookupElementStrings, "city_silence", "city_silent", "city_serenity")
    }

    @Test
    fun testCompletion_ForReferenceSnippetWithPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_city_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSize(3, lookupElementStrings)
        assertContainsElements(lookupElementStrings, "city_silence", "city_silent", "city_serenity")
    }

    @Test
    fun testCompletion_ForReferenceSnippetWithLongerPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_city_si<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSize(2, lookupElementStrings)
        assertContainsElements(lookupElementStrings, "city_silence", "city_silent")
    }

    // endregion
}
