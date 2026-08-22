package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
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
class IncorrectColumnSizeInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(IncorrectColumnSizeInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region rowTypeIsKey

    @Test
    fun rowTypeIsKey_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag
            k1;0;yes;red_flag
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_missing_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            val m1 = "Incorrect column size (row config: test_row_key, expect: 4, actual: 3)"
            """
            id;number;${error(m1)}status${errorEnd()}
            k1;0;${error(m1)}yes${errorEnd()}
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_unsorted_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;flag;status
            k1;0;red_flag;yes
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_unknown_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;unknown
            k1;0;yes;unknown
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_extraUnknown_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            val m1 = "Incorrect column size (row config: test_row_key, expect: 4, actual: 5)"
            """
            id;number;status;flag;${error(m1)}unknown${errorEnd()}
            k1;0;yes;red_flag;${error(m1)}unknown${errorEnd()}
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_mismatchedHeaderSize_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            val m1 = "Incorrect column size (row config: test_row_key, expect: 4, actual: 5)"
            """
            id;number;status;flag;${error(m1)}plus${errorEnd()}
            k1;0;yes;red_flag
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_mismatchedRowSize_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            val m1 = "Incorrect column size (row config: test_row_key, expect: 4, actual: 6)"
            """
            id;number;status;flag
            k1;0;yes;red_flag;plus;${error(m1)}plus${errorEnd()}
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_correct_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag
            k1;0;yes;red_flag
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_ignoreHeaderColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag;end_column
            k1;0;yes;red_flag;
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_ignoreRowColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag;end_column
            k1;0;yes;red_flag;123
            """.trimIndent()
        }
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
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_unsorted_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;flag;status;status
            k1;0;red_flag;yes;no
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_unknown_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv", """
             id;number;status;flag;unknown
            k1;0;yes;red_flag;unknown
        """.trimIndent())
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
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_correct_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status
            k1;0;yes;red_flag;no
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_ignoreHeaderColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status;end_column
            k1;0;yes;red_flag;no
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_ignoreRowColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag;status;end_column
            k1;0;yes;red_flag;no;123
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion

    // region ignored

    @Test
    fun noSemantic_ignored() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.csv")
        myFixture.configureByText("test.csv", """
            id;number;status;flag
            k1;0;yes;red_flag
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    // endregion
}
