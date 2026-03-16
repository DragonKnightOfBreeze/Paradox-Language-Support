package icu.windea.pls.lang.intentions.localisation

import com.intellij.testFramework.IndexingTestUtil
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
 * @see ChangeLocalisationColorIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ChangeLocalisationColorIntentionTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/intentions/localisation")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureColorFile() {
        markFileInfo(gameType, "interface/fonts.gfx")
        myFixture.configureByFile("features/intentions/localisation/interface/fonts.gfx")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    @Test
    fun testAvailable_onColorToken() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n key: \"§<caret>RRed text§!\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testNotAvailable_beforeColorMarker() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n key: \"<caret>§RRed text§!\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_outsideColorToken() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n key: \"<caret>Plain text\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_betweenColorTokens() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n key: \"§RRed§! <caret>§GGreen§!\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_onKey() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n <caret>key: \"§RRed text§!\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testAvailable_onBlueColor() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n key: \"§<caret>BBlue text§!\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_multipleTokensInLine() {
        configureColorFile()
        val intentionName = PlsBundle.message("intention.changeLocalisationColor")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("color.test.yml", "l_english:\n key: \"§RFirst§! and §<caret>GSecond§!\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }
}
