package icu.windea.pls.lang.inspections.overrides

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.HighlightingTestScope
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
 * @see OverrideForDefinitionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class OverrideForDefinitionInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, gameType)
        myFixture.enableInspections(OverrideForDefinitionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple_fios_armies() {
        val key = "defense_army"
        val tag = ChronicleBundle.message("inspection.overrideForDefinition.desc", key).toWeakWarningTag()

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
        val key = "defense_army"
        val tag = ChronicleBundle.message("inspection.overrideForDefinition.desc", key).toWeakWarningTag()

        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "common/armies/99_defense_armies.txt")
        myFixture.configureByText("99_defense_armies.txt", """
            ${tag.start}defense_army${tag.end} = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_samePath_armies() {
        val key = "defense_army"
        val tag = ChronicleBundle.message("inspection.overrideForDefinition.desc", key).toWeakWarningTag()

        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.txt", """
            defense_army = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        markFileInfo(gameType, "common/armies/01_defense_armies.txt")
        myFixture.configureByText("01_defense_armies.copy.txt", """
            ${tag.start}defense_army${tag.end} = {
	            defensive = yes
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun simple_fios_events() {
        val key = "test.1"
        val tag = ChronicleBundle.message("inspection.overrideForDefinition.desc", key).toWeakWarningTag()

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
            ${tag.start}event${tag.end} = {
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
        val tag = ChronicleBundle.message("inspection.overrideForDefinition.desc", key).toWeakWarningTag()

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
        val key = "test.1"
        val tag = ChronicleBundle.message("inspection.overrideForDefinition.desc", key).toWeakWarningTag()

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
            ${tag.start}event${tag.end} = {
                id = test.1
                # ...
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }
}
