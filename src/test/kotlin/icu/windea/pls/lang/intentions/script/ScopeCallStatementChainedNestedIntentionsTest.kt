package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
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
        markRootDirectory("features/intentions")
        markConfigDirectory("features/intentions/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Eu5)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Chained → Nested Form

    @Test
    fun testScopeCallToNestedForm_basic() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            "test_effect = { <caret>root.owner = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                <caret>root = {
                    owner = v
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_keepSeparator() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            "test_effect = { <caret>root.owner ?= v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                <caret>root ?= {
                    owner ?= v
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
            "chained_nested.test.txt",
            """test_effect = { <caret>"root.owner" = v }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                <caret>"root" = {
                    "owner" = v
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_withComments() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """
            test_effect = {
                # comment
                <caret>root.owner = v
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                # comment
                <caret>root = {
                    owner = v
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
            "chained_nested.test.txt",
            "test_effect = { root.<caret>owner = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root = {
                    owner = v
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
            "chained_nested.test.txt",
            """test_effect = { root<caret>.owner = v }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root<caret> = {
                    owner = v
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
            "chained_nested.test.txt",
            """test_effect = { "root.owner.<caret>event_target:x" = v }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                "root.owner" = {
                    "<caret>event_target:x" = v
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
            "chained_nested.test.txt",
            "test_effect = { root.owner.event_target:<caret>x = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_effect = {
                root.owner = {
                    event_target:<caret>x = v
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToNestedForm_notAvailableWhenNotScopeLink() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            "test_effect = { <caret>root.owner = v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToNestedForm_notAvailableWhenNotAssignOperator() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            "test_effect = { <caret>root.owner <= v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToNestedForm_notAvailableWhenSingleKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNestedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            "test_effect = { <caret>owner = v }"
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
            "chained_nested.test.txt",
            """test_effect = { <caret>root = { owner = v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("test_effect = { root.owner = v }")
    }

    @Test
    fun testScopeCallToChainedForm_keepSeparator() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """test_effect = { <caret>root ?= { owner ?= v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("test_effect = { root.owner ?= v }")
    }

    @Test
    fun testScopeCallToChainedForm_keepInnerSeparator() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """test_effect = { <caret>root = { owner ?= v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("test_effect = { root.owner ?= v }")
    }

    @Test
    fun testScopeCallToChainedForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """test_effect = { <caret>"root" = { "owner" = v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""test_effect = { "root.owner" = v }""")
    }

    @Test
    fun testScopeCallToChainedForm_withInnerQuotedKey() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """test_effect = { <caret>root = { "owner" = v } }"""
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""test_effect = { root.owner = v }""")
    }

    @Test
    fun testScopeCallToChainedForm_withComments() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """
            test_effect = {
                # comment1
                <caret>root = {
                    # comment2
                    owner = v # comment3
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
                root.owner = v
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToChainedForm_notAvailableWhenNotScopeLink() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """test_effect = { <caret>root = { owner = v } }"""
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToChainedForm_notAvailableWhenNotAssignOperator() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """test_effect = { <caret>root >= { owner = v } }"""
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToChainedForm_notAvailableWhenMultipleChildren() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/chained_nested.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToChainedForm")
        myFixture.configureByText(
            "chained_nested.test.txt",
            """
            test_effect = {
                <caret>root = {
                    owner = v
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
            "chained_nested.test.txt",
            "test_effect = { <caret>root = yes }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion
}
