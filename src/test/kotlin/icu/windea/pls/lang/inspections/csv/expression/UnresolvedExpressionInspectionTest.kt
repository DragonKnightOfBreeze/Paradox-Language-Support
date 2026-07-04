package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
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
 * @see UnresolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class UnresolvedExpressionInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region noSmantic

    @Test
    fun noSemantic_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            k1;0;yes;red_flag;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    // endregion

    // region rowTypeIsKey

    @Test
    fun rowTypeIsKey_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            k1;0;yes;red_flag;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_smoke_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            id_is;1.234;not;valid;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_smoke_lastRowIgnored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_row/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            k1;0;yes;red_flag;
            yes;it's;some;thing;to;be;ignored
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_smoke_lastColumnIgnored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;ignored;
            k1;0;yes;red_flag;ignored;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    // endregion

    // region rowTypeIsIndex

    @Test
    fun rowTypeIsIndex_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            k1;0;yes;red_flag;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_smoke_failed() {
        val tag1 = "Cannot resolve expression `1.234` (row config: test_row_index, expect matching: int[1..10])".toErrorTag()
        val tag2 = "Cannot resolve expression `not` (row config: test_row_index, expect matching: bool)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            id_is;1.234;not;valid;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_smoke_lastRowIgnored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_row/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;
            k1;0;yes;red_flag;
            yes;it's;some;thing;to;be;ignored
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_smoke_lastColumnIgnored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;ignored;
            k1;0;yes;red_flag;ignored;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    // endregion
}
