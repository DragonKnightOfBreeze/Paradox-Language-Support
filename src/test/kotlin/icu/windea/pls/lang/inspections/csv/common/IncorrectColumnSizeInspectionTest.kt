package icu.windea.pls.lang.inspections.csv.common

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
 * @see IncorrectColumnSizeInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class IncorrectColumnSizeInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(IncorrectColumnSizeInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region noSmantic

    @Test
    fun noSemantic_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag
            k1;0;yes;red_flag
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
            id;number;status;flag
            k1;0;yes;red_flag
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_missing_failed() {
        val tag = "Incorrect column size (row config: test_row_key, expect: 4, actual: 3)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;${tag.start}status${tag.end}
            k1;0;${tag.start}yes${tag.end}
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_unsorted_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;flag;status
            k1;0;red_flag;yes
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_unknown_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;unknown
            k1;0;yes;unknown
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_extraUnknown_failed() {
        val tag = "Incorrect column size (row config: test_row_key, expect: 4, actual: 5)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;${tag.start}unknown${tag.end}
            k1;0;yes;red_flag;${tag.start}unknown${tag.end}
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_mismatchedHeaderSize_failed() {
        val tag = "Incorrect column size (row config: test_row_key, expect: 4, actual: 5)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;${tag.start}plus${tag.end}
            k1;0;yes;red_flag
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_mismatchedRowSize_failed() {
        val tag = "Incorrect column size (row config: test_row_key, expect: 4, actual: 6)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag
            k1;0;yes;red_flag;plus;${tag.start}plus${tag.end}
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_correct_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag
            k1;0;yes;red_flag
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_ignoreHeaderColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;end_column
            k1;0;yes;red_flag;
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_ignoreRowColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;end_column
            k1;0;yes;red_flag;123
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
            id;number;status;flag;status
            k1;0;yes;red_flag;no
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_missing_failed() {
        val tag = "Incorrect column size (row config: test_row_index, expect: 5, actual: 3)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;${tag.start}status${tag.end}
            k1;0;${tag.start}yes${tag.end}
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_unsorted_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;flag;status;status
            k1;0;red_flag;yes;no
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_unknown_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
             id;number;status;flag;unknown
            k1;0;yes;red_flag;unknown
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_extraUnknown_failed() {
        val tag = "Incorrect column size (row config: test_row_index, expect: 5, actual: 6)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
             id;number;status;flag;status;${tag.start}unknown${tag.end}
            k1;0;yes;red_flag;no;${tag.start}unknown${tag.end}
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_mismatched1_failed() {
        val tag = "Incorrect column size (row config: test_row_index, expect: 5, actual: 6)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status;${tag.start}addon${tag.end}
            k1;0;yes;red_flag;no
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_mismatched2_failed() {
        val tag = "Incorrect column size (row config: test_row_index, expect: 5, actual: 7)".toErrorTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status
            k1;0;yes;red_flag;no;addon;${tag.start}another${tag.end}
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_correct_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status
            k1;0;yes;red_flag;no
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_ignoreHeaderColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status;end_column
            k1;0;yes;red_flag;no
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_ignoreRowColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status;end_column
            k1;0;yes;red_flag;no;123
        """.trimIndent())

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile)
        myFixture.checkHighlighting()
    }


    // endregion
}
