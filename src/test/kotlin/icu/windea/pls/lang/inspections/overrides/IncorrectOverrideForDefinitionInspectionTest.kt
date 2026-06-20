package icu.windea.pls.lang.inspections.overrides

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.HighlightingTestScope
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
 * @see IncorrectOverrideForDefinitionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectOverrideForDefinitionInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, gameType)
        myFixture.enableInspections(IncorrectOverrideForDefinitionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple_fios_armies() {
        val key = "defense_army"
        val overrideStrategy = ParadoxOverrideStrategy.LIOS
        val description = PlsBundle.message("inspection.incorrectOverrideForDefinition.desc", key, overrideStrategy)
        val tag = description.toWarningTag()

        markFileInfo(gameType, "common/armies/99_defense_armies.txt")
        myFixture.configureByText("99_defense_armies.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.txt", """
            ${tag.start}defense_army${tag.end} = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_lios_armies() {
        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "common/armies/99_defense_armies.txt")
        myFixture.configureByText("99_defense_armies.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_samePath_armies() {
        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.copy.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_fios_events() {
        markFileInfo(gameType, "events/99_test_events.txt")
        myFixture.configureByText("99_test_events.txt", """
            namespace = test
            event = {
                id = test.1
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.txt", """
            namespace = test
            event = {
                id = test.1
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_lios_events() {
        val key = "test.1"
        val overrideStrategy = ParadoxOverrideStrategy.FIOS
        val description = PlsBundle.message("inspection.incorrectOverrideForDefinition.desc", key, overrideStrategy)
        val tag = description.toWarningTag()

        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.txt", """
            namespace = test
            event = {
                id = test.1
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "events/99_test_events.txt")
        myFixture.configureByText("99_test_events.txt", """
            namespace = test
            ${tag.start}event${tag.end} = {
                id = test.1
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_samePath_events() {
        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.txt", """
            namespace = test
            event = {
                id = test.1
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.copy.txt", """
            namespace = test
            event = {
                id = test.1
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }
}
