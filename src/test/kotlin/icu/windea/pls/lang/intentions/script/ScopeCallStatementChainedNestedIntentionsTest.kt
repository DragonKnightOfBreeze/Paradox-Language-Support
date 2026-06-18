package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ScopeCallStatementToChainedFormIntention
 * @see ScopeCallStatementToNestedFormIntention
 * @see ParadoxScopeCallStatementManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ScopeCallStatementChainedNestedIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/intentions/script") // necessary here
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Chained → Nested Form

    @Test
    fun testScopeCallToNestedForm_basic() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { <caret>root.owner = { k = v } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                <caret>root = {
                    owner = { k = v }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>"root.owner" = { k = v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                <caret>"root" = {
                    "owner" = { k = v }
                }
            }
            """.trimIndent()
        )
    }


    @Test
    fun testScopeCallToNestedForm_caretOnSecond() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { root.<caret>owner = { k = v } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root = {
                    owner = { k = v }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_caretOnDot() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { root<caret>.owner = { k = v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root<caret> = {
                    owner = { k = v }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_caretOnThirdOfThree() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { "root.owner.<caret>event_target:x" = { k = v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                "root.owner" = {
                    "<caret>event_target:x" = { k = v }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_caretOnThirdOfThree_another() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { root.owner.event_target:<caret>x = { k = v } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root.owner = {
                    event_target:<caret>x = { k = v }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_notAvailableWhenSingleKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { <caret>owner = { k = v } }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToNestedForm_availableForQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>"root.owner" = { k = v } }"""
        )
        // 引号包围的键包含点号，可通过语法检查检测到链式形式
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    // endregion

    // region Nested → Chained Form

    @Test
    fun testScopeCallToChainedForm_basic() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>root = { owner = { k = v } } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("test_effect = { root.owner = { k = v } }")
    }

    @Test
    fun testScopeCallToChainedForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>"root" = { "owner" = { k = v } } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""test_effect = { "root.owner" = { k = v } }""")
    }

    @Test
    fun testScopeCallToChainedForm_withInnerQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>root = { "owner" = { k = v } } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""test_effect = { root.owner = { k = v } }""")
    }

    @Test
    fun testScopeCallToChainedForm_withComments() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """
            test_effect = {
                # comment1
                <caret>root = {
                    # comment2
                    owner = { k = v } # comment3
                    # comment4
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                # comment1
                # comment2
                # comment3
                # comment4
                root.owner = { k = v }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToChainedForm_notAvailableWhenMultipleChildren() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """
            test_effect = {
                <caret>root = {
                    owner = { k = v }
                    other = { a = b }
                }
            }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToChainedForm_notAvailableWhenNoBlock() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { <caret>root = yes }"
        )

        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion
}
