package icu.windea.pls.lang.inspections.script.statement

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.HighlightingTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ScopeCallStatementToExplicitFormInspection
 * @see ScopeCallStatementToSafeFormInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ScopeCallStatementInspectionsTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        myFixture.enableInspections(
            ScopeCallStatementToExplicitFormInspection::class.java,
            ScopeCallStatementToSafeFormInspection::class.java,
        )
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // NOTE not available for INFORMATION level
    // @Test
    // fun safeForm_ck3() {
    //     markFileInfo(ParadoxGameType.Ck3, "common/test/inspection_scope_call.ck3.test.txt")
    //     val description = PlsBundle.message("inspection.script.scopeCallStatementToExplicitForm.desc")
    //     val tag = description.toInfoTag()
    //
    //     myFixture.configureByText("inspection_scope_call.ck3.test.txt", """
    //         k = {
    //             ${tag.start}owner${tag.end} ?= v
    //         }
    //     """.trimIndent())
    //
    //     myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
    //     myFixture.checkHighlighting(true, true, true)
    // }

    @Test
    fun explicitForm_ck3() {
        markFileInfo(ParadoxGameType.Ck3, "common/test/inspection_scope_call.ck3.test.txt")
        val description = PlsBundle.message("inspection.script.scopeCallStatementToSafeForm.desc")
        val tag = description.toWeakWarningTag()

        myFixture.configureByText("inspection_scope_call.ck3.test.txt", """
            k = {
                exists = owner
                ${tag.start}owner${tag.end} = v
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun explicitForm_hoi4_noWarning() {
        markFileInfo(ParadoxGameType.Hoi4, "common/test/inspection_scope_call.hoi4.test.txt")

        myFixture.configureByText("inspection_scope_call.hoi4.test.txt", """
            k = {
                exists = owner
                owner = v
            }
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting(true, true, true)
    }
}
