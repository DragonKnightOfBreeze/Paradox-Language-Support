package icu.windea.pls.script.codeInsight.surroundWith

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
 * Paradox Script Surrounders 测试。
 *
 * @see ParadoxScriptSurroundDescriptor
 * @see ParadoxScriptSurrounder
 * @see ParadoxScriptPropertySurrounder
 * @see ParadoxScriptBlockSurrounder
 * @see ParadoxScriptParameterConditionSurrounder
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptSurroundersTest : BasePlatformTestCase() {
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
            ParadoxScriptPropertySurrounder(),
            "script/surroundWith/property_single_before.test.txt",
            "script/surroundWith/property_single_after.test.txt"
        )
    }

    @Test
    fun testPropertySurrounder_multipleElements() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptPropertySurrounder(),
            "script/surroundWith/property_multiple_before.test.txt",
            "script/surroundWith/property_multiple_after.test.txt"
        )
    }

    @Test
    fun testBlockSurrounder_singleElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptBlockSurrounder(),
            "script/surroundWith/block_single_before.test.txt",
            "script/surroundWith/block_single_after.test.txt"
        )
    }

    @Test
    fun testBlockSurrounder_multipleElements() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptBlockSurrounder(),
            "script/surroundWith/block_multiple_before.test.txt",
            "script/surroundWith/block_multiple_after.test.txt"
        )
    }

    @Test
    fun testParameterConditionSurrounder_singleElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptParameterConditionSurrounder(),
            "script/surroundWith/parameter_condition_single_before.test.txt",
            "script/surroundWith/parameter_condition_single_after.test.txt"
        )
    }

    @Test
    fun testParameterConditionSurrounder_multipleElements() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptParameterConditionSurrounder(),
            "script/surroundWith/parameter_condition_multiple_before.test.txt",
            "script/surroundWith/parameter_condition_multiple_after.test.txt"
        )
    }

    @Test
    fun testPropertySurrounder_nestedElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptPropertySurrounder(),
            "script/surroundWith/property_nested_before.test.txt",
            "script/surroundWith/property_nested_after.test.txt"
        )
    }

    @Test
    fun testPropertySurrounder_withComment() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptPropertySurrounder(),
            "script/surroundWith/property_with_comment_before.test.txt",
            "script/surroundWith/property_with_comment_after.test.txt"
        )
    }

    @Test
    fun testBlockSurrounder_withEmptyLines() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptBlockSurrounder(),
            "script/surroundWith/block_with_empty_lines_before.test.txt",
            "script/surroundWith/block_with_empty_lines_after.test.txt"
        )
    }

    @Test
    fun testParameterConditionSurrounder_nestedElement() {
        CodeInsightTestUtil.doSurroundWithTest(
            myFixture,
            ParadoxScriptParameterConditionSurrounder(),
            "script/surroundWith/parameter_condition_nested_before.test.txt",
            "script/surroundWith/parameter_condition_nested_after.test.txt"
        )
    }
}
