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
 * @see UnresolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class UnresolvedExpressionInspectionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
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
        myFixture.checkHighlighting()
    }

    // endregion

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
    fun rowTypeIsKey_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key/test.csv")
        myFixture.configureByText("test.csv") {
            val m1 = "Cannot resolve column expression `1.234` (expect matching: int[0..10])"
            val m2 = "Cannot resolve column expression `not` (expect matching: bool)"
            """
            id;number;status;flag;status
            id_is;${error(m1)}1.234${errorEnd()};${error(m2)}not${errorEnd()};valid;yes
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastRow_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_row/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag
            k1;0;yes;red_flag
            yes;it's;some;thing;to;be;ignored
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsKey_skipLastColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/key_skip_last_column/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag;ignored
            k1;0;yes;red_flag;ignored
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion

    // region rowTypeIsIndex

    @Test
    fun rowTypeIsIndex_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag;status
            k1;0;yes;red_flag;no
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_failed() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index/test.csv")
        myFixture.configureByText("test.csv") {
            val m1 = "Cannot resolve column expression `1.234` (expect matching: int[0..10])"
            val m2 = "Cannot resolve column expression `not` (expect matching: bool)"
            """
            id;number;status;flag;status
            id_is;${error(m1)}1.234${errorEnd()};${error(m2)}not${errorEnd()};valid;no
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastRow_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_row/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag;status
            k1;0;yes;red_flag;no
            yes;it's;some;thing;to;be;ignored
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun rowTypeIsIndex_skipLastColumn_success() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_rows/index_skip_last_column/test.csv")
        myFixture.configureByText("test.csv") {
            """
            id;number;status;flag;status;ignored
            k1;0;yes;red_flag;no;ignored
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // endregion
}
