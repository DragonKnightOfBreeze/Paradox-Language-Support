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
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Chained → Nested Form

    @Test
    fun testScopeCallToNestedForm_caretOnFirst() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { <caret>root.owner = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root = {
                    owner = { a = 1 }
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
            "test_effect = { root.<caret>owner = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root = {
                    owner = { a = 1 }
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
            "test_effect = { root<caret>.owner = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root = {
                    owner = { a = 1 }
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
            "test_effect = { root.owner.<caret>target = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root.owner = {
                    target = { a = 1 }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_oneLine() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "<caret>root.owner = { a = 1 }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            root = {
                owner = { a = 1 }
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
            "test_effect = { <caret>owner = { a = 1 } }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToNestedForm_notAvailableWhenQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>"root.owner" = { a = 1 } }"""
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion

    // region Nested → Chained Form

    @Test
    fun testScopeCallToChainedForm_basic() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>root = { owner = { a = 1 } } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("test_effect = { root.owner = { a = 1 } }")
    }

    @Test
    fun testScopeCallToChainedForm_oneLine() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "<caret>root = { owner = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("root.owner = { a = 1 }")
    }

    @Test
    fun testScopeCallToChainedForm_withCommentBefore() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """
            test_effect = {
                <caret>root = {
                    # comment
                    owner = { a = 1 }
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                # comment
                root.owner = { a = 1 }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToChainedForm_withCommentAfter() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """
            test_effect = {
                <caret>root = {
                    owner = { a = 1 }
                    # comment after
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                # comment after
                root.owner = { a = 1 }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToChainedForm_withBothComments() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """
            test_effect = {
                <caret>root = {
                    # comment before
                    owner = { a = 1 }
                    # comment after
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                # comment before
                # comment after
                root.owner = { a = 1 }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToChainedForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            """test_effect = { <caret>"root" = { "owner" = { a = 1 } } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""test_effect = { "root.owner" = { a = 1 } }""")
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
                    owner = { a = 1 }
                    other = { b = 2 }
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

    @Test
    fun testScopeCallToChainedForm_availableOnInnerKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested_stellaris.test.txt",
            "test_effect = { root = { <caret>owner = { a = 1 } } }"
        )
        // 内层属性也是有效的嵌套形式目标，意向应当可用
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    // endregion
}
