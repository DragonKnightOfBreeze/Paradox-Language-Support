package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
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
 * @see ScopeCallStatementToNormalFormIntention
 * @see ScopeCallStatementToSafeFormIntention
 * @see ParadoxScopeCallStatementManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ScopeCallStatementNormalSafeIntentionsTest : BasePlatformTestCase() {
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

    // region Safe → Normal Form

    @Test
    fun testScopeCallToNormalForm_basic() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner ?= v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            "test_effect = { owner = v }"
        )
    }

    @Test
    fun testScopeCallToNormalForm_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner? = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            "test_effect = { owner = v }"
        )
    }

    @Test
    fun testScopeCallToNormalForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>\"owner\" ?= v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            "test_effect = { \"owner\" = v }"
        )
    }

    @Test
    fun testScopeCallToNormalForm_notAvailableWhenNotScopeLink() {
        markFileInfo(ParadoxGameType.Eu5, "common/test/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner ?= v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToNormalForm_notAvailableWhenNotAssignOperator() {
        markFileInfo(ParadoxGameType.Eu5, "common/test/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner <= v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToNormalForm_notAvailableWhenAlreadyNormalForm() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner = v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion

    // region Normal → Safe Form

    @Test
    fun testScopeCallToSafeForm_basic() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            "test_effect = { owner ?= v }"
        )
    }

    @Test
    fun testScopeCallToSafeForm_stellaris() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            "test_effect = { owner? = v }"
        )
    }

    @Test
    fun testScopeCallToSafeForm_withExists() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_triggers/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            """
            test_trigger = {
                exists = owner
                <caret>owner = v
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_trigger = {
                owner ?= v
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToSafeForm_withMultiExists() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_triggers/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            """
            test_trigger = {
                # comment1
                exists = this
                # comment2
                exists = from
                exists = from.owner
                exists = owner
                # comment3
                <caret>from.owner = v
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            test_trigger = {
                # comment1
                exists = this
                # comment2
                exists = owner
                # comment3
                from.owner ?= v
            }
            """.trimIndent()
        )
    }

    @Test
    fun testScopeCallToSafeForm_withQuotedKey() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>\"owner\" = v }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            "test_effect = { \"owner\" ?= v }"
        )
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenNotScopeLink() {
        markFileInfo(ParadoxGameType.Eu5, "common/test/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner = v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenNotAssignOperator() {
        markFileInfo(ParadoxGameType.Eu5, "common/test/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner <= v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenAlreadySafeForm() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner ?= v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testScopeCallToSafeForm_notAvailableWhenAlreadySafeCallForm() {
        markFileInfo(ParadoxGameType.Eu5, "common/scripted_effects/normal_safe.test.txt")
        val intentionName = ChronicleBundle.message("intention.scopeCallStatementToSafeForm")
        myFixture.configureByText(
            "normal_safe.test.txt",
            "test_effect = { <caret>owner? = v }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion
}
