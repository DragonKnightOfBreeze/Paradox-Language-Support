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
 * @see ScopeCallStatementToNormalFormIntention
 * @see ScopeCallStatementToSafeFormIntention
 * @see ParadoxScopeCallStatementManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ScopeCallStatementIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Safe → Normal Form (CK3-style ?=)

    @Test
    fun testScopeCallToNormalForm_basic() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_normal_form_basic.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "to_normal_form_basic.ck3.test.txt",
            "k = { <caret>owner ?= { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { exists = owner owner = { a = 1 } }")
    }

    @Test
    fun testScopeCallToNormalForm_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/to_normal_form_stellaris.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "to_normal_form_stellaris.test.txt",
            "k = { <caret>owner? = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { exists = owner owner = { a = 1 } }")
    }

    @Test
    fun testScopeCallToNormalForm_withPrecedingComment() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_normal_form_with_comment.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "to_normal_form_with_comment.ck3.test.txt",
            "k = {\n    # some comment\n    <caret>owner ?= {\n        a = 1\n    }\n}"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = {\n    # some comment\n    exists = owner\n    owner = {\n        a = 1\n    }\n}")
    }

    @Test
    fun testScopeCallToNormalForm_notAvailableWhenAlreadyNormalForm() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_normal_form_not_available.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "to_normal_form_not_available.ck3.test.txt",
            "k = { exists = owner\n<caret>owner = { a = 1 } }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion

    // region Normal → Safe Form (CK3-style ?=)

    @Test
    fun testScopeCallToSafeForm_basic() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_basic.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_basic.ck3.test.txt",
            "k = { exists = owner\n<caret>owner = { a = 1 } }"
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
            "k = {\n    exists = owner\n    # comment between\n    <caret>owner = {\n        a = 1\n    }\n}"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = {\n    # comment between\n    owner ?= {\n        a = 1\n    }\n}")
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
            "k = { exists = owner\n<caret>other = { a = 1 } }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/to_safe_form_quoted.test.txt")
        val intentionName = PlsBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "to_safe_form_quoted.ck3.test.txt",
            "k = { exists = owner\n<caret>\"owner\" = { a = 1 } }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { \"owner\" ?= { a = 1 } }")
    }

    // endregion
}
