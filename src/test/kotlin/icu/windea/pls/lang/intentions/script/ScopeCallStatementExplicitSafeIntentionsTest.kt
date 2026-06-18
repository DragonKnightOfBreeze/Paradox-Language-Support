package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ScopeCallStatementToExplicitFormIntention
 * @see ScopeCallStatementToSafeFormIntention
 * @see ParadoxScopeCallStatementManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ScopeCallStatementExplicitSafeIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Safe → Normal Form

    @Test
    fun testScopeCallToExplicitForm_basic() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_normal_form_basic.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToExplicitForm")
        myFixture.configureByText(
            "to_normal_form_basic.ck3.test.txt",
            "k = { <caret>owner ?= { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { exists = owner owner = { a = 1 } }")
    }

    @Test
    fun testScopeCallToExplicitForm_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/to_normal_form_stellaris.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToExplicitForm")
        myFixture.configureByText(
            "to_normal_form_stellaris.test.txt",
            "k = { <caret>owner? = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { exists = owner owner = { a = 1 } }")
    }

    @Test
    fun testScopeCallToExplicitForm_withPrecedingComment() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_normal_form_with_comment.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToExplicitForm")
        myFixture.configureByText(
            "to_normal_form_with_comment.ck3.test.txt",
            """
            k = {
                # some comment
                <caret>owner ?= {
                    a = 1
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            k = {
                # some comment
                exists = owner
                owner = {
                    a = 1
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToExplicitForm_notAvailableWhenAlreadyExplicitForm() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_normal_form_not_available.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToExplicitForm")
        myFixture.configureByText(
            "to_normal_form_not_available.ck3.test.txt",
            """
            k = { exists = owner
            <caret>owner = { a = 1 } }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion

    // region Normal → Safe Form

    @Test
    fun testScopeCallToSafeForm_basic() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_basic.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_basic.ck3.test.txt",
            """
            k = { exists = owner
            <caret>owner = { a = 1 } }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { owner ?= { a = 1 } }")
    }

    @Test
    fun testScopeCallToSafeForm_basic_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/to_safe_form_basic_stellaris.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_basic_stellaris.test.txt",
            """
            k = { exists = owner
            <caret>owner = { a = 1 } }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { owner? = { a = 1 } }")
    }

    @Test
    fun testScopeCallToSafeForm_oneLine() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_basic.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_basic.ck3.test.txt",
            "k = { exists = owner <caret>owner = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { owner ?= { a = 1 } }")
    }

    @Test
    fun testScopeCallToSafeForm_oneLine_quoted() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_basic.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_basic.ck3.test.txt",
            "k = { exists = \"owner\" <caret>owner = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { owner ?= { a = 1 } }")
    }

    @Test
    fun testScopeCallToSafeForm_withCommentBetween() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_with_comment.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_with_comment.ck3.test.txt",
            """
            k = {
                exists = owner
                # comment between
                <caret>owner = {
                    a = 1
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            k = {
                # comment between
                owner ?= {
                    a = 1
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToSafeForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_quoted.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_quoted.ck3.test.txt",
            """
            k = { exists = owner
            <caret>"owner" = { a = 1 } }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""k = { "owner" ?= { a = 1 } }""")
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenAlreadySafeForm() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_not_available_already_safe.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_not_available_already_safe.ck3.test.txt",
            "k = { <caret>owner ?= { a = 1 } }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenMismatchKey() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_not_available_mismatch.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_not_available_mismatch.ck3.test.txt",
            """
            k = { exists = owner
            <caret>other = { a = 1 } }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenNotNeighboring() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_not_available_notNeighboring.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_not_available_notNeighboring.ck3.test.txt",
            """
            k = {
                exists = owner
                key = value
                <caret>owner = { a = 1 }
            }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenNotBefore() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_not_available_notBefore.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_not_available_notBefore.ck3.test.txt",
            """
            k = {
                <caret>owner = { a = 1 }
                exists = owner
            }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion
}
