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
 * @see MismatchedEventIdInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class MismatchedEventIdInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Eu5)
        myFixture.enableInspections(MismatchedEventIdInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Sequential (Stellaris) - Matched

    @Test
    fun stellaris_matchedSimple() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            event = {
                id = foo.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_matchedMultipleEvents() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            event = {
                id = foo.1
            }
            event = {
                id = foo.2
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_matchedNamespaceSwitch() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            event = {
                id = foo.1
            }
            namespace = bar
            event = {
                id = bar.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region Sequential (Stellaris) - Mismatched (desc.2)

    @Test
    fun stellaris_mismatchedSingle() {
        val eventId = "bar.1"
        val namespace = "foo"
        val tag = ChronicleBundle.message("inspection.script.mismatchedEventId.desc.2", eventId, namespace).toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            event = {
                id = ${tag.start}bar.1${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun stellaris_mismatchedAfterSwitch() {
        val eventId = "foo.2"
        val namespace = "bar"
        val tag = ChronicleBundle.message("inspection.script.mismatchedEventId.desc.2", eventId, namespace).toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            event = {
                id = foo.1
            }
            namespace = bar
            event = {
                id = ${tag.start}foo.2${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region Sequential (Stellaris) - No Binding (desc.1)

    @Test
    fun stellaris_noNamespace() {
        val eventId = "foo.1"
        val tag = ChronicleBundle.message("inspection.script.mismatchedEventId.desc.1", eventId).toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            event = {
                id = ${tag.start}foo.1${tag.end}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region Mixed (EU5) - Matched

    @Test
    fun eu5_matchedSimple() {
        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            foo.1 = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun eu5_matchedMultipleNamespaces() {
        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            namespace = bar
            foo.1 = {
                type = country_event
            }
            bar.1 = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region Mixed (EU5) - No Binding (desc.1)

    @Test
    fun eu5_unboundNoMatchingNamespace() {
        val eventId = "bar.1"
        val tag = ChronicleBundle.message("inspection.script.mismatchedEventId.desc.1", eventId).toWarningTag()

        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = foo
            ${tag.start}bar.1${tag.end} = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun eu5_unboundNoNamespace() {
        val eventId = "foo.1"
        val tag = ChronicleBundle.message("inspection.script.mismatchedEventId.desc.1", eventId).toWarningTag()

        markFileInfo(ParadoxGameType.Eu5, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            ${tag.start}foo.1${tag.end} = {
                type = country_event
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion
}
