package icu.windea.pls.cwt.codeInsight.surroundWith

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestUtil
import icu.windea.pls.test.addAdditionalAllowedRoots
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * CWT Surrounders 测试。
 *
 * @see CwtSurroundDescriptor
 * @see CwtSurrounder
 * @see CwtPropertySurrounder
 * @see CwtBlockSurrounder
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtSurroundersTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        addAdditionalAllowedRoots(testDataPath)
        markIntegrationTest()
    }

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testPropertySurrounder_singleElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtPropertySurrounder(),
            "cwt/surroundWith/property_single_before.test.cwt",
            "cwt/surroundWith/property_single_after.test.cwt"
        )
    }

    @Test
    fun testPropertySurrounder_multipleElements() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtPropertySurrounder(),
            "cwt/surroundWith/property_multiple_before.test.cwt",
            "cwt/surroundWith/property_multiple_after.test.cwt"
        )
    }

    @Test
    fun testBlockSurrounder_singleElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtBlockSurrounder(),
            "cwt/surroundWith/block_single_before.test.cwt",
            "cwt/surroundWith/block_single_after.test.cwt"
        )
    }

    @Test
    fun testBlockSurrounder_multipleElements() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtBlockSurrounder(),
            "cwt/surroundWith/block_multiple_before.test.cwt",
            "cwt/surroundWith/block_multiple_after.test.cwt"
        )
    }

    @Test
    fun testPropertySurrounder_nestedElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtPropertySurrounder(),
            "cwt/surroundWith/property_nested_before.test.cwt",
            "cwt/surroundWith/property_nested_after.test.cwt"
        )
    }

    @Test
    fun testPropertySurrounder_withComment() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtPropertySurrounder(),
            "cwt/surroundWith/property_with_comment_before.test.cwt",
            "cwt/surroundWith/property_with_comment_after.test.cwt"
        )
    }

    @Test
    fun testBlockSurrounder_withEmptyLines() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            CwtBlockSurrounder(),
            "cwt/surroundWith/block_with_empty_lines_before.test.cwt",
            "cwt/surroundWith/block_with_empty_lines_after.test.cwt"
        )
    }
}
