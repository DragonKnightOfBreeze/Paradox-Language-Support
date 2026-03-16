package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
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
 * @see ChangeDefinitionInjectionModeIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ChangeDefinitionInjectionModeIntentionTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Vic3

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/intentions/definition_injection")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testAvailable_onInjectPrefix() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("inject.test.txt", "<caret>INJECT:factory = { size = 5 }")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onInjectColon() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("inject.test.txt", "INJECT<caret>:factory = { size = 5 }")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onInjectTarget() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("inject.test.txt", "INJECT:fac<caret>tory = { size = 5 }")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testNotAvailable_onEquals() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("inject.test.txt", "INJECT:factory <caret>= { size = 5 }")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_onValue() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("inject.test.txt", "INJECT:factory = { <caret>size = 5 }")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_regularProperty() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("regular.test.txt", "<caret>factory = { size = 5 }")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_unsupportedGameType() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(ParadoxGameType.Stellaris, "common/buildings/00_buildings.txt")
        myFixture.configureByText("inject.test.txt", "<caret>INJECT:factory = { size = 5 }")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testAvailable_onReplaceMode() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("replace.test.txt", "<caret>REPLACE:factory = { size = 5 }")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onTryInjectMode() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("try_inject.test.txt", "<caret>TRY_INJECT:factory = { size = 5 }")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onReplaceOrCreateMode() {
        val intentionName = PlsBundle.message("intention.changeDefinitionInjectionMode")
        markFileInfo(gameType, "common/buildings/00_buildings.txt")
        myFixture.configureByText("replace_or_create.test.txt", "<caret>REPLACE_OR_CREATE:my_building = { size = 10 }")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }
}
