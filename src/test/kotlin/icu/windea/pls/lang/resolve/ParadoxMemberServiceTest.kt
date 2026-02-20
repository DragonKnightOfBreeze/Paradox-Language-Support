package icu.windea.pls.lang.resolve

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.*
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.util.builders.ParadoxScriptTextBuilder.parameter as p

/**
 * 为 [ParadoxMemberService] 中的各公开方法提供全面的测试用例。
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxMemberServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    // region Helper Methods

    private fun configureScriptFile(text: String): ParadoxScriptFile {
        myFixture.configureByText("test.txt", text.trimIndent())
        return myFixture.file as ParadoxScriptFile
    }

    private fun findProperty(file: ParadoxScriptFile, name: String): ParadoxScriptProperty {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptProperty::class.java)
            .firstOrNull { it.name == name }
            ?: throw AssertionError("Property not found: $name")
    }

    private fun findAllProperties(file: ParadoxScriptFile, name: String): List<ParadoxScriptProperty> {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptProperty::class.java)
            .filter { it.name == name }
    }

    private fun findBlockValue(file: ParadoxScriptFile, text: String): ParadoxScriptValue {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptValue::class.java)
            .firstOrNull { it.value == text && it.isBlockMember() }
            ?: throw AssertionError("Block member value not found: $text")
    }

    private fun findAllBlockValues(file: ParadoxScriptFile): List<ParadoxScriptValue> {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptValue::class.java)
            .filter { it.isBlockMember() }
    }

    private fun findScriptedVariable(file: ParadoxScriptFile, name: String): ParadoxScriptScriptedVariable {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptScriptedVariable::class.java)
            .firstOrNull { it.name == name }
            ?: throw AssertionError("Scripted variable not found: @$name")
    }

    // endregion

    // region getPath Tests

    @Test
    fun getPath_rootLevelProperty_returnsPropertyName() {
        val file = configureScriptFile("root_prop = 1")
        val prop = findProperty(file, "root_prop")
        val path = ParadoxMemberService.getPath(prop)
        Assert.assertEquals("root_prop", path!!.path)
        Assert.assertEquals(1, path.length)
    }

    @Test
    fun getPath_nestedProperty_returnsFullPath() {
        val file = configureScriptFile(
            """
            a = {
                b = {
                    c = {
                        d = 1
                    }
                }
            }
            """
        )
        val d = findProperty(file, "d")
        val path = ParadoxMemberService.getPath(d)
        Assert.assertEquals("a/b/c/d", path!!.path)
        Assert.assertEquals(listOf("a", "b", "c", "d"), path.subPaths)
    }

    @Test
    fun getPath_blockMemberValue_usesHyphen() {
        val file = configureScriptFile(
            """
            list = { "item1" "item2" "item3" }
            """
        )
        val item1 = findBlockValue(file, "item1")
        val path = ParadoxMemberService.getPath(item1)
        Assert.assertEquals("list/-", path!!.path)
    }

    @Test
    fun getPath_mixedPropertiesAndValues_correctPath() {
        val file = configureScriptFile(
            """
            outer = {
                inner = {
                    "value1"
                    "value2"
                }
            }
            """
        )
        val value1 = findBlockValue(file, "value1")
        val path = ParadoxMemberService.getPath(value1)
        Assert.assertEquals("outer/inner/-", path!!.path)
    }

    @Test
    fun getPath_withLimit_returnsLimitedPath() {
        val file = configureScriptFile(
            """
            a = { b = { c = { d = { e = 1 } } } }
            """
        )
        val e = findProperty(file, "e")

        // limit = 2，只返回最近的 2 层
        val limited = ParadoxMemberService.getPath(e, limit = 2)
        Assert.assertEquals("d/e", limited!!.path)
        Assert.assertEquals(2, limited.length)

        // limit = 3
        val limited3 = ParadoxMemberService.getPath(e, limit = 3)
        Assert.assertEquals("c/d/e", limited3!!.path)
    }

    @Test
    fun getPath_withMaxDepth_returnsNullIfExceeded() {
        val file = configureScriptFile(
            """
            a = { b = { c = { d = 1 } } }
            """
        )
        val d = findProperty(file, "d")

        // maxDepth 检查逻辑：在添加元素前，若 maxDepth <= result.size 则返回 null
        // 对于 4 层路径 "a/b/c/d"：
        // - maxDepth = 2：添加 d(size=1), 添加 c(size=2), 尝试添加 b 时 2<=2 返回 null
        // - maxDepth = 3：添加 d, c, b(size=3), 尝试添加 a 时 3<=3 返回 null
        // - maxDepth = 4：添加 d, c, b, a(size=4), 正常返回
        val result2 = ParadoxMemberService.getPath(d, maxDepth = 2)
        Assert.assertNull(result2)

        val result3 = ParadoxMemberService.getPath(d, maxDepth = 3)
        Assert.assertNull(result3)

        // maxDepth = 4，刚好允许 4 层，正常返回
        val result4 = ParadoxMemberService.getPath(d, maxDepth = 4)
        Assert.assertEquals("a/b/c/d", result4!!.path)

        // maxDepth = 5，大于实际深度，正常返回
        val result5 = ParadoxMemberService.getPath(d, maxDepth = 5)
        Assert.assertEquals("a/b/c/d", result5!!.path)
    }

    @Test
    fun getPath_parameterizedKey_parameterAwareTrue_returnsPath() {
        val file = configureScriptFile("root = { \"key_${p("PARAM")}\" = 1 }")
        val props = findAllProperties(file, "key_\$PARAM\$")
        Assert.assertTrue(props.isNotEmpty())
        val prop = props.first()
        val path = ParadoxMemberService.getPath(prop, parameterAware = true)
        Assert.assertNotNull(path)
        Assert.assertEquals("root/key_\$PARAM\$", path!!.path)
    }

    @Test
    fun getPath_parameterizedKey_parameterAwareFalse_returnsNull() {
        val file = configureScriptFile("root = { \"key_${p("PARAM")}\" = 1 }")
        val props = findAllProperties(file, "key_\$PARAM\$")
        Assert.assertTrue(props.isNotEmpty())
        val prop = props.first()
        val path = ParadoxMemberService.getPath(prop, parameterAware = false)
        Assert.assertNull(path)
    }

    @Test
    fun getPath_fileElement_returnsEmptyPath() {
        val file = configureScriptFile("root = 1")
        val path = ParadoxMemberService.getPath(file)
        Assert.assertEquals("", path!!.path)
        Assert.assertTrue(path.isEmpty())
    }

    @Test
    fun getPath_propertyValue_returnsPropertyPath() {
        val file = configureScriptFile("outer = { inner = value }")
        val inner = findProperty(file, "inner")
        // 获取属性值
        val value = inner.propertyValue
        Assert.assertNotNull(value)
        // 值本身的路径应该与属性相同
        val path = ParadoxMemberService.getPath(value!!)
        Assert.assertEquals("outer/inner", path!!.path)
    }

    @Test
    fun getPath_multipleBlockValues_allHaveSamePath() {
        val file = configureScriptFile(
            """
            items = {
                1
                2
                3
            }
            """
        )
        val values = findAllBlockValues(file)
        Assert.assertEquals(3, values.size)
        for (v in values) {
            val path = ParadoxMemberService.getPath(v)
            Assert.assertEquals("items/-", path!!.path)
        }
    }

    @Test
    fun getPath_quotedPropertyKey_stripsQuotes() {
        val file = configureScriptFile(
            """
            "quoted key" = {
                nested = 1
            }
            """
        )
        val nested = findProperty(file, "nested")
        val path = ParadoxMemberService.getPath(nested)
        Assert.assertEquals("quoted key/nested", path!!.path)
    }

    @Test
    fun getPath_keyWithSlash_escapedInPath() {
        val file = configureScriptFile(
            """
            "key/with/slash" = {
                nested = 1
            }
            """
        )
        val nested = findProperty(file, "nested")
        val path = ParadoxMemberService.getPath(nested)
        // 路径中的 "/" 应被转义为 "\/"
        Assert.assertEquals("key\\/with\\/slash/nested", path!!.path)
    }

    // endregion

    // region getRootKeys Tests

    @Test
    fun getRootKeys_rootLevelProperty_returnsEmptyList() {
        val file = configureScriptFile("root_prop = 1")
        val prop = findProperty(file, "root_prop")
        val rootKeys = ParadoxMemberService.getRootKeys(prop)
        // 根级属性没有父键
        Assert.assertTrue(rootKeys!!.isEmpty())
    }

    @Test
    fun getRootKeys_nestedProperty_returnsParentKeys() {
        val file = configureScriptFile(
            """
            a = {
                b = {
                    c = 1
                }
            }
            """
        )
        val c = findProperty(file, "c")
        val rootKeys = ParadoxMemberService.getRootKeys(c)
        // 不包含 c 自身
        Assert.assertEquals(listOf("a", "b"), rootKeys)
    }

    @Test
    fun getRootKeys_blockMemberValue_returnsParentKeys() {
        val file = configureScriptFile(
            """
            list = {
                nested = {
                    "item"
                }
            }
            """
        )
        val item = findBlockValue(file, "item")
        val rootKeys = ParadoxMemberService.getRootKeys(item)
        Assert.assertEquals(listOf("list", "nested"), rootKeys)
    }

    @Test
    fun getRootKeys_withLimit_returnsLimitedKeys() {
        val file = configureScriptFile(
            """
            a = { b = { c = { d = 1 } } }
            """
        )
        val d = findProperty(file, "d")
        val limited = ParadoxMemberService.getRootKeys(d, limit = 2)
        Assert.assertEquals(listOf("b", "c"), limited)
    }

    @Test
    fun getRootKeys_withMaxDepth_returnsNullIfExceeded() {
        val file = configureScriptFile(
            """
            a = { b = { c = { d = 1 } } }
            """
        )
        val d = findProperty(file, "d")
        // d 的父键是 a, b, c，共 3 个
        val result = ParadoxMemberService.getRootKeys(d, maxDepth = 2)
        Assert.assertNull(result)
    }

    @Test
    fun getRootKeys_parameterizedParent_parameterAwareFalse_returnsNull() {
        val file = configureScriptFile("\"parent_${p("P")}\" = { child = 1 }")
        val child = findProperty(file, "child")
        val rootKeys = ParadoxMemberService.getRootKeys(child, parameterAware = false)
        Assert.assertNull(rootKeys)
    }

    @Test
    fun getRootKeys_fileElement_returnsEmptyList() {
        val file = configureScriptFile("root = 1")
        val rootKeys = ParadoxMemberService.getRootKeys(file)
        Assert.assertTrue(rootKeys!!.isEmpty())
    }

    // endregion

    // region getKeyPrefixes Tests

    @Test
    fun getKeyPrefixes_withPrecedingStrings_returnsPrefixes() {
        val file = configureScriptFile(
            """
            block = {
                "prefix1"
                "prefix2"
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertEquals(listOf("prefix1", "prefix2"), prefixes)
    }

    @Test
    fun getKeyPrefixes_skipsCommentsAndWhitespace() {
        val file = configureScriptFile(
            """
            block = {
                "p1"
                # comment between
                "p2"
                
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertEquals(listOf("p1", "p2"), prefixes)
    }

    @Test
    fun getKeyPrefixes_stopsAtNonStringElement() {
        val file = configureScriptFile(
            """
            block = {
                "early"
                other_prop = 1
                "late"
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        // 只返回 target 之前连续的字符串，遇到 other_prop 就停止
        Assert.assertEquals(listOf("late"), prefixes)
    }

    @Test
    fun getKeyPrefixes_noPrecedingStrings_returnsEmptyList() {
        val file = configureScriptFile(
            """
            block = {
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertTrue(prefixes!!.isEmpty())
    }

    @Test
    fun getKeyPrefixes_withLimit_returnsLimitedPrefixes() {
        val file = configureScriptFile(
            """
            block = {
                "p1"
                "p2"
                "p3"
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val limited = ParadoxMemberService.getKeyPrefixes(target, limit = 2)
        // limit=2，从后往前取 2 个
        Assert.assertEquals(listOf("p2", "p3"), limited)
    }

    @Test
    fun getKeyPrefixes_withMaxDepth_returnsNullIfExceeded() {
        val file = configureScriptFile(
            """
            block = {
                "p1"
                "p2"
                "p3"
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val result = ParadoxMemberService.getKeyPrefixes(target, maxDepth = 2)
        Assert.assertNull(result)
    }

    @Test
    fun getKeyPrefixes_parameterizedString_stopsCollection() {
        val file = configureScriptFile("block = { \"p1\" \"p_${p("X")}\" \"p3\" target = 1 }")
        val target = findProperty(file, "target")
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        // 遇到参数化字符串时停止收集
        Assert.assertEquals(listOf("p3"), prefixes)
    }

    @Test
    fun getKeyPrefixes_fileElement_returnsEmptyList() {
        val file = configureScriptFile("root = 1")
        val prefixes = ParadoxMemberService.getKeyPrefixes(file)
        Assert.assertTrue(prefixes!!.isEmpty())
    }

    @Test
    fun getKeyPrefixes_forBlockValue_returnsPrecedingStrings() {
        val file = configureScriptFile(
            """
            block = {
                "prefix"
                "target_value"
            }
            """
        )
        val targetValue = findBlockValue(file, "target_value")
        val prefixes = ParadoxMemberService.getKeyPrefixes(targetValue)
        Assert.assertEquals(listOf("prefix"), prefixes)
    }

    // endregion

    // region getKeyPrefix Tests

    @Test
    fun getKeyPrefix_withSinglePrefix_returnsIt() {
        val file = configureScriptFile(
            """
            block = {
                "single_prefix"
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertEquals("single_prefix", prefix)
    }

    @Test
    fun getKeyPrefix_withMultiplePrefixes_returnsLastOne() {
        val file = configureScriptFile(
            """
            block = {
                "first"
                "second"
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertEquals("second", prefix)
    }

    @Test
    fun getKeyPrefix_noPrefixes_returnsNull() {
        val file = configureScriptFile(
            """
            block = {
                target = 1
            }
            """
        )
        val target = findProperty(file, "target")
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertNull(prefix)
    }

    @Test
    fun getKeyPrefix_parameterizedPrefix_returnsNull() {
        val file = configureScriptFile("block = { \"p_${p("X")}\" target = 1 }")
        val target = findProperty(file, "target")
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertNull(prefix)
    }

    // endregion

    // region Injected Root Keys Tests

    @Test
    fun injectRootKeys_affectsGetPath() {
        val file = configureScriptFile("a = { b = 1 }")
        val virtualFile = file.virtualFile!!
        ParadoxAnalysisInjector.injectRootKeys(virtualFile, listOf("injected"))

        val b = findProperty(file, "b")
        val path = ParadoxMemberService.getPath(b)
        Assert.assertEquals("injected/a/b", path!!.path)

        ParadoxAnalysisInjector.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_affectsGetRootKeys() {
        val file = configureScriptFile("a = { b = 1 }")
        val virtualFile = file.virtualFile!!
        ParadoxAnalysisInjector.injectRootKeys(virtualFile, listOf("i1", "i2"))

        val b = findProperty(file, "b")
        val rootKeys = ParadoxMemberService.getRootKeys(b)
        Assert.assertEquals(listOf("i1", "i2", "a"), rootKeys)

        ParadoxAnalysisInjector.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_doesNotAffectLimitedPath() {
        val file = configureScriptFile("a = { b = { c = 1 } }")
        val virtualFile = file.virtualFile!!
        ParadoxAnalysisInjector.injectRootKeys(virtualFile, listOf("injected"))

        val c = findProperty(file, "c")
        // limit=2 时，只返回最近 2 层，不受注入键影响
        val limited = ParadoxMemberService.getPath(c, limit = 2)
        Assert.assertEquals("b/c", limited!!.path)

        ParadoxAnalysisInjector.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_multipleInjected_allPrepended() {
        val file = configureScriptFile("root = { child = 1 }")
        val virtualFile = file.virtualFile!!
        ParadoxAnalysisInjector.injectRootKeys(virtualFile, listOf("i1", "i2", "i3"))

        val child = findProperty(file, "child")
        val path = ParadoxMemberService.getPath(child)
        Assert.assertEquals("i1/i2/i3/root/child", path!!.path)

        ParadoxAnalysisInjector.injectRootKeys(virtualFile, emptyList())
    }

    // endregion

    // region Edge Cases and Special Values

    @Test
    fun getPath_scriptedVariable_returnsEmptyPath() {
        val file = configureScriptFile("@my_var = 42")
        val variable = findScriptedVariable(file, "my_var")
        // 封装变量不是 ParadoxScriptProperty 或 ParadoxScriptValue，应返回空路径
        val path = ParadoxMemberService.getPath(variable)
        Assert.assertEquals("", path!!.path)
    }

    @Test
    fun getPath_colorValue_correctPath() {
        val file = configureScriptFile(
            """
            settings = {
                color = rgb { 255 128 64 }
            }
            """
        )
        val color = findProperty(file, "color")
        val path = ParadoxMemberService.getPath(color)
        Assert.assertEquals("settings/color", path!!.path)
    }

    @Test
    fun getPath_booleanValue_correctPath() {
        val file = configureScriptFile(
            """
            config = {
                enabled = yes
                disabled = no
            }
            """
        )
        val enabled = findProperty(file, "enabled")
        val disabled = findProperty(file, "disabled")
        Assert.assertEquals("config/enabled", ParadoxMemberService.getPath(enabled)!!.path)
        Assert.assertEquals("config/disabled", ParadoxMemberService.getPath(disabled)!!.path)
    }

    @Test
    fun getPath_numericValues_correctPath() {
        val file = configureScriptFile(
            """
            numbers = {
                int_val = 42
                float_val = 3.14
                negative = -10
            }
            """
        )
        Assert.assertEquals("numbers/int_val", ParadoxMemberService.getPath(findProperty(file, "int_val"))!!.path)
        Assert.assertEquals("numbers/float_val", ParadoxMemberService.getPath(findProperty(file, "float_val"))!!.path)
        Assert.assertEquals("numbers/negative", ParadoxMemberService.getPath(findProperty(file, "negative"))!!.path)
    }

    @Test
    fun getPath_scriptedVariableReference_correctPath() {
        val file = configureScriptFile(
            """
            @cost = 100
            item = {
                price = @cost
            }
            """
        )
        val price = findProperty(file, "price")
        val path = ParadoxMemberService.getPath(price)
        Assert.assertEquals("item/price", path!!.path)
    }

    @Test
    fun getPath_inlineMath_correctPath() {
        val file = configureScriptFile(
            """
            calc = {
                result = @[ 1 + 2 * 3 ]
            }
            """
        )
        val result = findProperty(file, "result")
        val path = ParadoxMemberService.getPath(result)
        Assert.assertEquals("calc/result", path!!.path)
    }

    @Test
    fun getPath_emptyBlock_propertyStillHasPath() {
        val file = configureScriptFile(
            """
            empty = { }
            """
        )
        val empty = findProperty(file, "empty")
        val path = ParadoxMemberService.getPath(empty)
        Assert.assertEquals("empty", path!!.path)
    }

    @Test
    fun getPath_deeplyNested_correctPath() {
        val file = configureScriptFile(
            """
            l1 = { l2 = { l3 = { l4 = { l5 = { l6 = { l7 = { l8 = { l9 = { l10 = 1 } } } } } } } } }
            """
        )
        val l10 = findProperty(file, "l10")
        val path = ParadoxMemberService.getPath(l10)
        Assert.assertEquals("l1/l2/l3/l4/l5/l6/l7/l8/l9/l10", path!!.path)
        Assert.assertEquals(10, path.length)
    }

    @Test
    fun getPath_siblingProperties_differentPaths() {
        val file = configureScriptFile(
            """
            parent = {
                child1 = 1
                child2 = 2
                child3 = 3
            }
            """
        )
        Assert.assertEquals("parent/child1", ParadoxMemberService.getPath(findProperty(file, "child1"))!!.path)
        Assert.assertEquals("parent/child2", ParadoxMemberService.getPath(findProperty(file, "child2"))!!.path)
        Assert.assertEquals("parent/child3", ParadoxMemberService.getPath(findProperty(file, "child3"))!!.path)
    }

    @Test
    fun getPath_multipleRootProperties_eachHasOwnPath() {
        val file = configureScriptFile(
            """
            root1 = 1
            root2 = 2
            root3 = { nested = 1 }
            """
        )
        Assert.assertEquals("root1", ParadoxMemberService.getPath(findProperty(file, "root1"))!!.path)
        Assert.assertEquals("root2", ParadoxMemberService.getPath(findProperty(file, "root2"))!!.path)
        Assert.assertEquals("root3", ParadoxMemberService.getPath(findProperty(file, "root3"))!!.path)
        Assert.assertEquals("root3/nested", ParadoxMemberService.getPath(findProperty(file, "nested"))!!.path)
    }

    @Test
    fun getPath_mixedBlockContent_correctPaths() {
        val file = configureScriptFile(
            """
            mixed = {
                prop1 = a
                "value1"
                prop2 = b
                "value2"
            }
            """
        )
        Assert.assertEquals("mixed/prop1", ParadoxMemberService.getPath(findProperty(file, "prop1"))!!.path)
        Assert.assertEquals("mixed/prop2", ParadoxMemberService.getPath(findProperty(file, "prop2"))!!.path)
        Assert.assertEquals("mixed/-", ParadoxMemberService.getPath(findBlockValue(file, "value1"))!!.path)
        Assert.assertEquals("mixed/-", ParadoxMemberService.getPath(findBlockValue(file, "value2"))!!.path)
    }

    // endregion

    // region ParadoxMemberPath Operations

    @Test
    fun memberPath_equality() {
        val path1 = ParadoxMemberPath.resolve("a/b/c")
        val path2 = ParadoxMemberPath.resolve(listOf("a", "b", "c"))
        Assert.assertEquals(path1, path2)
    }

    @Test
    fun memberPath_emptyPath() {
        val empty1 = ParadoxMemberPath.resolveEmpty()
        val empty2 = ParadoxMemberPath.resolve("")
        val empty3 = ParadoxMemberPath.resolve(emptyList())
        Assert.assertEquals(empty1, empty2)
        Assert.assertEquals(empty2, empty3)
        Assert.assertTrue(empty1.isEmpty())
    }

    @Test
    fun memberPath_subPaths() {
        val path = ParadoxMemberPath.resolve("a/b/c")
        Assert.assertEquals(listOf("a", "b", "c"), path.subPaths)
        Assert.assertEquals(3, path.length)
        Assert.assertEquals("a", path.get(0))
        Assert.assertEquals("b", path.get(1))
        Assert.assertEquals("c", path.get(2))
        Assert.assertEquals("", path.get(3)) // out of bounds returns empty
    }

    // endregion
}
