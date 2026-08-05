package icu.windea.pls.test.chronicle

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.inspections.script.common.DuplicateScriptedVariablesInspection
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * 用作展示的平台测试。
 *
 * @see ChronicleTestScope
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ChronicleScopedTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("chronicle")
        markConfigDirectory("chronicle/.config") // showcase only, not actually required for this test case
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun the_waker_test() {
        markFileInfo(ParadoxGameType.Stellaris, "common/tests/greetings/00_greetings.txt")
        myFixture.configureByText("00_greetings.txt", """
            the_waker = {
                <caret>on_written = {
                    do = seeking_and_explonating
                }
            }
        """.trimIndent())

        val property = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        assertEquals("on_written", property.name)
    }

    @Test
    fun the_waked_test() {
        // `<caret>` should be after `@`
        markFileInfo(ParadoxGameType.Stellaris, "common/tests/greetings/01_greetings.txt")
        myFixture.configureByText("01_greetings.txt", """
            @answer = 42
            @answer = NaN
            @expected_answer = "greetings"
            the_waked = {
                on_waked = {
                    do = answer
                    value = @<caret>answer
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val reference = myFixture.findReferenceAtCaret()?.castOrNull<ParadoxScriptedVariablePsiReference>()!!
        assertEquals("NaN", reference.resolve()?.value)
    }

    @Test
    fun the_waked_completion_test() {
        // `<caret>` should be after `@`
        markFileInfo(ParadoxGameType.Stellaris, "common/tests/greetings/01_greetings.txt")
        myFixture.configureByText("01_greetings.txt", """
            @answer = 42
            @answer = NaN
            @expected_answer = "greetings"
            the_waked = {
                on_waked = {
                    do = answer
                    value = @<caret>
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "answer", "expected_answer")
    }

    @Test
    fun the_waked_highlighting_test() {
        myFixture.enableInspections(DuplicateScriptedVariablesInspection::class.java)

        val tag = ChronicleBundle.message("inspection.script.duplicateScriptedVariables.desc", "answer").toWarningTag()

        // tag markers should be surrounding `@v`, rather than `@v = v`
        markFileInfo(ParadoxGameType.Stellaris, "common/tests/greetings")
        myFixture.configureByText("01_greetings.txt", """
            ${tag.start}@answer${tag.end} = 42
            ${tag.start}@answer${tag.end} = NaN
            @expected_answer = "greetings"
            the_waked = {
                on_waked = {
                    do = answer
                    value = @answer
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.checkHighlighting()
    }
}
