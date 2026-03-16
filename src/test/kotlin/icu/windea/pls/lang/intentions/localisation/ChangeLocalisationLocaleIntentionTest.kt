package icu.windea.pls.lang.intentions.localisation

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
 * @see ChangeLocalisationLocaleIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ChangeLocalisationLocaleIntentionTest : BasePlatformTestCase() {
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

    @Test
    fun testAvailable_onLocaleToken() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "<caret>l_english:\n key: \"value\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onLocaleTokenMiddle() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "l_eng<caret>lish:\n key: \"value\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onLocaleTokenEnd() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "l_english<caret>:\n key: \"value\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testNotAvailable_afterColon() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "l_english:<caret>\n key: \"value\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_onKey() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "l_english:\n <caret>key: \"value\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailable_onValue() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "l_english:\n key: \"<caret>value\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testAvailable_onGermanLocale() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "<caret>l_german:\n key: \"value\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_onSimpChineseLocale() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", "<caret>l_simp_chinese:\n key: \"value\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }

    @Test
    fun testAvailable_multipleLocalesInFile() {
        val intentionName = PlsBundle.message("intention.changeLocalisationLocale")
        markFileInfo(gameType, "localisation/test.yml")
        myFixture.configureByText("locale.test.yml", 
            "l_english:\n key: \"English\"\n\n<caret>l_german:\n key: \"Deutsch\"")
        val intention = myFixture.findSingleIntention(intentionName)
        assertNotNull(intention)
    }
}
