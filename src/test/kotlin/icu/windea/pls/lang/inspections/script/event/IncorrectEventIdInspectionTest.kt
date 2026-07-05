package icu.windea.pls.lang.inspections.script.event

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
 * @see IncorrectEventIdInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectEventIdInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Eu5)
        myFixture.enableInspections(IncorrectEventIdInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Stellaris - Valid

    @Test
    fun stellaris_validBasic() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = test.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_validZero() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = test.0
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_validLeadingZero() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = test.01
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_validUnderscore() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = my_event
            event = {
                id = my_event.42
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region Stellaris - Invalid

    @Test
    fun stellaris_invalidNoDot() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = ${tag.start}test${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_invalidEmptyPrefix() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", ".1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = ${tag.start}.1${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_invalidEmptyNumber() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test.").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = ${tag.start}test.${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_invalidNonNumeric() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test.abc").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = ${tag.start}test.abc${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_invalidHyphen() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test-name.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = ${tag.start}test-name.1${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_invalidMultipleDots() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test.1.2").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            event = {
                id = ${tag.start}test.1.2${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region EU5 - Valid

    @Test
    fun eu5_validBasic() {
        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            test.1 = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun eu5_validZero() {
        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            test.0 = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region EU5 - Invalid

    @Test
    fun eu5_invalidNoDot() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test").toWarningTag()

        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            ${tag.start}test${tag.end} = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun eu5_invalidHyphen() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventId.desc", "test-name.1").toWarningTag()

        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = test
            ${tag.start}test-name.1${tag.end} = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion
}
