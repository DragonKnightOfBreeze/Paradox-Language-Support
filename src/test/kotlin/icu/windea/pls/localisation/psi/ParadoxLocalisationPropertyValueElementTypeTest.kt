package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * 验证 [ParadoxLocalisationPropertyValueElementType] 的懒解析行为和 PSI 层级关系。
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationPropertyValueElementTypeTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureFile(): PsiFile {
        return myFixture.configureByFile("localisation/psi/property_value_element_type.test.yml")
    }

    private fun findPropertyByKey(file: PsiFile, key: String): ParadoxLocalisationProperty {
        val properties = PsiTreeUtil.findChildrenOfType(file, ParadoxLocalisationProperty::class.java)
        return properties.find { it.name == key }
            ?: throw AssertionError("Property '$key' not found in file")
    }

    // region PSI 层级验证

    /**
     * 从 [ParadoxLocalisationText] 向上验证完整的 PSI 层级关系。
     *
     * 预期层级（自底向上）：
     * - [ParadoxLocalisationText] (elementType=TEXT, language=ParadoxLocalisation)
     * - [LazyParseablePsiElement] (elementType=PROPERTY_VALUE_TOKEN, language=ParadoxLocalisation)
     * - [ParadoxLocalisationPropertyValue] (elementType=PROPERTY_VALUE, language=ParadoxLocalisation)
     * - [ParadoxLocalisationProperty] (elementType=PROPERTY, language=ParadoxLocalisation)
     * - [ParadoxLocalisationPropertyList] (elementType=PROPERTY_LIST, language=ParadoxLocalisation)
     * - [ParadoxLocalisationFile] (language=ParadoxLocalisation)
     */
    private fun assertTextElementHierarchy(textElement: ParadoxLocalisationText) {
        // TEXT 节点本身
        Assert.assertEquals(ParadoxLocalisationElementTypes.TEXT, textElement.node.elementType)
        Assert.assertEquals(ParadoxLocalisationLanguage, textElement.language)

        // PROPERTY_VALUE_TOKEN（LazyParseablePsiElement）
        val propertyValueToken = textElement.parent
        Assert.assertNotNull("Parent of TEXT should not be null", propertyValueToken)
        Assert.assertTrue(
            "Parent of TEXT should be LazyParseablePsiElement, got: ${propertyValueToken!!.javaClass.simpleName}",
            propertyValueToken is LazyParseablePsiElement
        )
        Assert.assertEquals(ParadoxLocalisationElementTypes.PROPERTY_VALUE_TOKEN, propertyValueToken.node.elementType)
        Assert.assertEquals(ParadoxLocalisationLanguage, propertyValueToken.language)

        // PROPERTY_VALUE
        val propertyValue = propertyValueToken.parent
        Assert.assertNotNull("Parent of PROPERTY_VALUE_TOKEN should not be null", propertyValue)
        Assert.assertTrue(
            "Parent of PROPERTY_VALUE_TOKEN should be ParadoxLocalisationPropertyValue, got: ${propertyValue!!.javaClass.simpleName}",
            propertyValue is ParadoxLocalisationPropertyValue
        )
        Assert.assertEquals(ParadoxLocalisationElementTypes.PROPERTY_VALUE, propertyValue.node.elementType)
        Assert.assertEquals(ParadoxLocalisationLanguage, propertyValue.language)

        // PROPERTY
        val property = propertyValue.parent
        Assert.assertNotNull("Parent of PROPERTY_VALUE should not be null", property)
        Assert.assertTrue(
            "Parent of PROPERTY_VALUE should be ParadoxLocalisationProperty, got: ${property!!.javaClass.simpleName}",
            property is ParadoxLocalisationProperty
        )
        Assert.assertEquals(ParadoxLocalisationElementTypes.PROPERTY, property.node.elementType)
        Assert.assertEquals(ParadoxLocalisationLanguage, property.language)

        // PROPERTY_LIST
        val propertyList = property.parent
        Assert.assertNotNull("Parent of PROPERTY should not be null", propertyList)
        Assert.assertTrue(
            "Parent of PROPERTY should be ParadoxLocalisationPropertyList, got: ${propertyList!!.javaClass.simpleName}",
            propertyList is ParadoxLocalisationPropertyList
        )
        Assert.assertEquals(ParadoxLocalisationElementTypes.PROPERTY_LIST, propertyList.node.elementType)
        Assert.assertEquals(ParadoxLocalisationLanguage, propertyList.language)

        // FILE
        val file = propertyList.parent
        Assert.assertNotNull("Parent of PROPERTY_LIST should not be null", file)
        Assert.assertTrue(
            "Parent of PROPERTY_LIST should be ParadoxLocalisationFile, got: ${file!!.javaClass.simpleName}",
            file is ParadoxLocalisationFile
        )
        Assert.assertEquals(ParadoxLocalisationLanguage, file.language)
    }

    // endregion

    // region 纯文本（不包含特殊标记）

    @Test
    fun testPlainEmpty_noPropertyValueToken() {
        val file = configureFile()
        val property = findPropertyByKey(file, "plain_empty")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        // 空字符串没有 PROPERTY_VALUE_TOKEN
        val tokenElement = propertyValue!!.tokenElement
        Assert.assertNull("Empty value should have no PROPERTY_VALUE_TOKEN", tokenElement)
        // richTextList 应为空
        Assert.assertTrue(propertyValue.richTextList.isEmpty())
    }

    @Test
    fun testPlainText_singleTextElement() {
        val file = configureFile()
        val property = findPropertyByKey(file, "plain_text")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertEquals("Plain text should have exactly 1 rich text element", 1, richTextList.size)
        val textElement = richTextList[0]
        Assert.assertTrue("Element should be ParadoxLocalisationText", textElement is ParadoxLocalisationText)
        Assert.assertEquals("Hello World", textElement.text)
        assertTextElementHierarchy(textElement as ParadoxLocalisationText)
    }

    @Test
    fun testPlainWhitespace_singleTextElement() {
        val file = configureFile()
        val property = findPropertyByKey(file, "plain_whitespace")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertEquals(1, richTextList.size)
        val textElement = richTextList[0]
        Assert.assertTrue(textElement is ParadoxLocalisationText)
        Assert.assertEquals("  ", textElement.text)
        assertTextElementHierarchy(textElement as ParadoxLocalisationText)
    }

    @Test
    fun testPlainMultiline_singleTextElement() {
        val file = configureFile()
        val property = findPropertyByKey(file, "plain_multiline")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertEquals(1, richTextList.size)
        val textElement = richTextList[0]
        Assert.assertTrue(textElement is ParadoxLocalisationText)
        Assert.assertEquals("Line one\\nLine two", textElement.text)
        assertTextElementHierarchy(textElement as ParadoxLocalisationText)
    }

    @Test
    fun testPlainWithBracketClose_singleTextElement() {
        val file = configureFile()
        val property = findPropertyByKey(file, "plain_with_bracket_close")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertEquals(1, richTextList.size)
        val textElement = richTextList[0]
        Assert.assertTrue(textElement is ParadoxLocalisationText)
        Assert.assertEquals("text]more", textElement.text)
    }

    // endregion

    // region 富文本（包含特殊标记）

    @Test
    fun testRichColorful_containsColorfulText() {
        val file = configureFile()
        val property = findPropertyByKey(file, "rich_colorful")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertTrue("Should contain colorful text elements", richTextList.size > 1 || richTextList.any { it is ParadoxLocalisationColorfulText })
    }

    @Test
    fun testRichParameter_containsParameter() {
        val file = configureFile()
        val property = findPropertyByKey(file, "rich_parameter")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertTrue("Should contain parameter elements", richTextList.any { it is ParadoxLocalisationParameter })
    }

    @Test
    fun testRichCommand_containsCommand() {
        val file = configureFile()
        val property = findPropertyByKey(file, "rich_command")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertTrue("Should contain command elements", richTextList.any { it is ParadoxLocalisationCommand })
    }

    @Test
    fun testRichIcon_containsIcon() {
        val file = configureFile()
        val property = findPropertyByKey(file, "rich_icon")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        Assert.assertTrue("Should contain icon elements", richTextList.any { it is ParadoxLocalisationIcon })
    }

    @Test
    fun testRichMixed_hierarchyCorrect() {
        val file = configureFile()
        val property = findPropertyByKey(file, "rich_mixed")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        val richTextList = propertyValue!!.richTextList
        // 混合富文本应包含多个元素
        Assert.assertTrue("Mixed rich text should have multiple elements", richTextList.size > 1)
        // 找到其中的 ParadoxLocalisationText 元素并验证层级
        val textElements = richTextList.filterIsInstance<ParadoxLocalisationText>()
        Assert.assertTrue("Should contain at least one text element", textElements.isNotEmpty())
        assertTextElementHierarchy(textElements.first())
    }

    // endregion

    // region 转义字符

    @Test
    fun testPlainWithEscape_containsEscapedMarker() {
        val file = configureFile()
        val property = findPropertyByKey(file, "plain_with_escape")
        val propertyValue = property.propertyValue
        Assert.assertNotNull(propertyValue)
        // 带转义的 $ 仍然会触发完整解析（保守策略），但结果仍然只有纯文本
        val tokenElement = propertyValue!!.tokenElement
        Assert.assertNotNull("Should have PROPERTY_VALUE_TOKEN", tokenElement)
    }

    // endregion
}
