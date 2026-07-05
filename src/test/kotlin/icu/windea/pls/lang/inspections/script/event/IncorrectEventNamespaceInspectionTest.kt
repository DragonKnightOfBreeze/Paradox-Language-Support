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
 * @see IncorrectEventNamespaceInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectEventNamespaceInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(IncorrectEventNamespaceInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Valid

    @Test
    fun validBasic() {
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
    fun validUnderscore() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = my_namespace
            event = {
                id = test.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun validMixedCase() {
        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = Test123
            event = {
                id = test.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region Invalid

    @Test
    fun invalidHyphen() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventNamespace.desc", "my-namespace").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = ${tag.start}my-namespace${tag.end}
            event = {
                id = test.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun invalidDot() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventNamespace.desc", "namespace.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = ${tag.start}namespace.1${tag.end}
            event = {
                id = test.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun invalidSpace() {
        val tag = ChronicleBundle.message("inspection.script.incorrectEventNamespace.desc", "my namespace").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "events/test_events.txt")
        myFixture.configureByText("test_events.txt", """
            namespace = ${tag.start}"my namespace"${tag.end}
            event = {
                id = test.1
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion
}
