package icu.windea.pls.lang.resolve

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDirectValue
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.text.ParadoxScriptTextBuilder.parameter as p

/**
 * @see ParadoxMemberService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxMemberServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getPath Tests

    @Test
    fun getPath_rootLevelProperty_returnsPropertyName() {
        myFixture.configureByText("test.txt", "<caret>root_prop = 1")
        val property = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(property)
        Assert.assertEquals("root_prop", path!!.path)
        Assert.assertEquals(1, path.length)
    }

    @Test
    fun getPath_nestedProperty_returnsFullPath() {
        myFixture.configureByText("test.txt", """
            a = {
                b = {
                    c = {
                        <caret>d = 1
                    }
                }
            }
        """)
        val d = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(d)
        Assert.assertEquals("a/b/c/d", path!!.path)
        Assert.assertEquals(listOf("a", "b", "c", "d"), path.subPaths)
    }

    @Test
    fun getPath_directMemberValue_usesHyphen() {
        myFixture.configureByText("test.txt", """
            list = { <caret>"item1" "item2" "item3" }
        """)
        val item1 = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        val path = ParadoxMemberService.getPath(item1)
        Assert.assertEquals("list/-", path!!.path)
    }

    @Test
    fun getPath_mixedPropertiesAndValues_correctPath() {
        myFixture.configureByText("test.txt", """
            outer = {
                inner = {
                    <caret>"value1"
                    "value2"
                }
            }
        """)
        val value1 = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        val path = ParadoxMemberService.getPath(value1)
        Assert.assertEquals("outer/inner/-", path!!.path)
    }

    @Test
    fun getPath_withLimit_returnsLimitedPath() {
        myFixture.configureByText("test.txt", """
            a = { b = { c = { d = { <caret>e = 1 } } } }
        """)
        val e = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!

        val limited = ParadoxMemberService.getPath(e, limit = 2)
        Assert.assertEquals("d/e", limited!!.path)
        Assert.assertEquals(2, limited.length)

        val limited3 = ParadoxMemberService.getPath(e, limit = 3)
        Assert.assertEquals("c/d/e", limited3!!.path)
    }

    @Test
    fun getPath_withMaxDepth_returnsNullIfExceeded() {
        myFixture.configureByText("test.txt", """
            a = { b = { c = { <caret>d = 1 } } }
        """)
        val d = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!

        val result2 = ParadoxMemberService.getPath(d, maxDepth = 2)
        Assert.assertNull(result2)

        val result3 = ParadoxMemberService.getPath(d, maxDepth = 3)
        Assert.assertNull(result3)

        val result4 = ParadoxMemberService.getPath(d, maxDepth = 4)
        Assert.assertEquals("a/b/c/d", result4!!.path)

        val result5 = ParadoxMemberService.getPath(d, maxDepth = 5)
        Assert.assertEquals("a/b/c/d", result5!!.path)
    }

    @Test
    fun getPath_parameterizedKey_parameterAwareTrue_returnsPath() {
        myFixture.configureByText("test.txt", "root = { <caret>\"key_${p("PARAM")}\" = 1 }")
        val prop = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(prop, parameterAware = true)
        Assert.assertNotNull(path)
        Assert.assertEquals("root/key_${p("PARAM")}", path!!.path)
    }

    @Test
    fun getPath_parameterizedKey_parameterAwareFalse_returnsNull() {
        myFixture.configureByText("test.txt", "root = { <caret>\"key_${p("PARAM")}\" = 1 }")
        val prop = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(prop, parameterAware = false)
        Assert.assertNull(path)
    }

    @Test
    fun getPath_fileElement_returnsEmptyPath() {
        myFixture.configureByText("test.txt", "<caret>root = 1")
        val file = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptFile>()!!
        val path = ParadoxMemberService.getPath(file)
        Assert.assertEquals("", path!!.path)
        Assert.assertTrue(path.isEmpty())
    }

    @Test
    fun getPath_propertyValue_returnsPropertyPath() {
        myFixture.configureByText("test.txt", "outer = { <caret>inner = value }")
        val inner = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val value = inner.propertyValue
        Assert.assertNotNull(value)
        val path = ParadoxMemberService.getPath(value!!)
        Assert.assertEquals("outer/inner", path!!.path)
    }

    @Test
    fun getPath_multipleDirectValues_allHaveSamePath() {
        myFixture.configureByText("test.txt", """
            items = {
                1
                <caret>2
                3
            }
        """)
        val caretValue = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        Assert.assertEquals("items/-", ParadoxMemberService.getPath(caretValue)!!.path)
        val values = PsiTreeUtil.findChildrenOfType(myFixture.file, ParadoxScriptValue::class.java)
            .filter { it.isDirectValue() }
        Assert.assertEquals(3, values.size)
        for (v in values) {
            Assert.assertEquals("items/-", ParadoxMemberService.getPath(v)!!.path)
        }
    }

    @Test
    fun getPath_quotedPropertyKey_stripsQuotes() {
        myFixture.configureByText("test.txt", """
            "quoted key" = {
                <caret>nested = 1
            }
        """)
        val nested = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(nested)
        Assert.assertEquals("quoted key/nested", path!!.path)
    }

    @Test
    fun getPath_keyWithSlash_escapedInPath() {
        myFixture.configureByText("test.txt", """
            "key/with/slash" = {
                <caret>nested = 1
            }
        """)
        val nested = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(nested)
        Assert.assertEquals("key\\/with\\/slash/nested", path!!.path)
    }

    // endregion

    // region getRootKeys Tests

    @Test
    fun getRootKeys_rootLevelProperty_returnsEmptyList() {
        myFixture.configureByText("test.txt", "<caret>root_prop = 1")
        val prop = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val rootKeys = ParadoxMemberService.getRootKeys(prop)
        Assert.assertTrue(rootKeys!!.isEmpty())
    }

    @Test
    fun getRootKeys_nestedProperty_returnsParentKeys() {
        myFixture.configureByText("test.txt", """
            a = {
                b = {
                    <caret>c = 1
                }
            }
        """)
        val c = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val rootKeys = ParadoxMemberService.getRootKeys(c)
        Assert.assertEquals(listOf("a", "b"), rootKeys)
    }

    @Test
    fun getRootKeys_directMemberValue_returnsParentKeys() {
        myFixture.configureByText("test.txt", """
            list = {
                nested = {
                    <caret>"item"
                }
            }
        """)
        val item = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        val rootKeys = ParadoxMemberService.getRootKeys(item)
        Assert.assertEquals(listOf("list", "nested"), rootKeys)
    }

    @Test
    fun getRootKeys_withLimit_returnsLimitedKeys() {
        myFixture.configureByText("test.txt", """
            a = { b = { c = { <caret>d = 1 } } }
        """)
        val d = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val limited = ParadoxMemberService.getRootKeys(d, limit = 2)
        Assert.assertEquals(listOf("b", "c"), limited)
    }

    @Test
    fun getRootKeys_withMaxDepth_returnsNullIfExceeded() {
        myFixture.configureByText("test.txt", """
            a = { b = { c = { <caret>d = 1 } } }
        """)
        val d = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val result = ParadoxMemberService.getRootKeys(d, maxDepth = 2)
        Assert.assertNull(result)
    }

    @Test
    fun getRootKeys_parameterizedParent_parameterAwareFalse_returnsNull() {
        myFixture.configureByText("test.txt", "\"parent_${p("P")}\" = { <caret>child = 1 }")
        val child = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val rootKeys = ParadoxMemberService.getRootKeys(child, parameterAware = false)
        Assert.assertNull(rootKeys)
    }

    @Test
    fun getRootKeys_fileElement_returnsEmptyList() {
        myFixture.configureByText("test.txt", "<caret>root = 1")
        val file = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptFile>()!!
        val rootKeys = ParadoxMemberService.getRootKeys(file)
        Assert.assertTrue(rootKeys!!.isEmpty())
    }

    // endregion

    // region getKeyPrefixes Tests

    @Test
    fun getKeyPrefixes_withPrecedingStrings_returnsPrefixes() {
        myFixture.configureByText("test.txt", """
            block = {
                "prefix1"
                "prefix2"
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertEquals(listOf("prefix1", "prefix2"), prefixes)
    }

    @Test
    fun getKeyPrefixes_skipsCommentsAndWhitespace() {
        myFixture.configureByText("test.txt", """
            block = {
                "p1"
                # comment between
                "p2"

                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertEquals(listOf("p1", "p2"), prefixes)
    }

    @Test
    fun getKeyPrefixes_stopsAtNonStringElement() {
        myFixture.configureByText("test.txt", """
            block = {
                "early"
                other_prop = 1
                "late"
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertEquals(listOf("late"), prefixes)
    }

    @Test
    fun getKeyPrefixes_noPrecedingStrings_returnsEmptyList() {
        myFixture.configureByText("test.txt", """
            block = {
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertTrue(prefixes!!.isEmpty())
    }

    @Test
    fun getKeyPrefixes_withLimit_returnsLimitedPrefixes() {
        myFixture.configureByText("test.txt", """
            block = {
                "p1"
                "p2"
                "p3"
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val limited = ParadoxMemberService.getKeyPrefixes(target, limit = 2)
        Assert.assertEquals(listOf("p2", "p3"), limited)
    }

    @Test
    fun getKeyPrefixes_withMaxDepth_returnsNullIfExceeded() {
        myFixture.configureByText("test.txt", """
            block = {
                "p1"
                "p2"
                "p3"
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val result = ParadoxMemberService.getKeyPrefixes(target, maxDepth = 2)
        Assert.assertNull(result)
    }

    @Test
    fun getKeyPrefixes_parameterizedString_stopsCollection() {
        myFixture.configureByText("test.txt", "block = { \"p1\" \"p_${p("X")}\" \"p3\" <caret>target = 1 }")
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(target)
        Assert.assertEquals(listOf("p3"), prefixes)
    }

    @Test
    fun getKeyPrefixes_fileElement_returnsEmptyList() {
        myFixture.configureByText("test.txt", "<caret>root = 1")
        val file = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptFile>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(file)
        Assert.assertTrue(prefixes!!.isEmpty())
    }

    @Test
    fun getKeyPrefixes_forDirectValue_returnsPrecedingStrings() {
        myFixture.configureByText("test.txt", """
            block = {
                "prefix"
                <caret>"target_value"
            }
        """)
        val targetValue = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        val prefixes = ParadoxMemberService.getKeyPrefixes(targetValue)
        Assert.assertEquals(listOf("prefix"), prefixes)
    }

    // endregion

    // region getKeyPrefix Tests

    @Test
    fun getKeyPrefix_withSinglePrefix_returnsIt() {
        myFixture.configureByText("test.txt", """
            block = {
                "single_prefix"
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertEquals("single_prefix", prefix)
    }

    @Test
    fun getKeyPrefix_withMultiplePrefixes_returnsLastOne() {
        myFixture.configureByText("test.txt", """
            block = {
                "first"
                "second"
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertEquals("second", prefix)
    }

    @Test
    fun getKeyPrefix_noPrefixes_returnsNull() {
        myFixture.configureByText("test.txt", """
            block = {
                <caret>target = 1
            }
        """)
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertNull(prefix)
    }

    @Test
    fun getKeyPrefix_parameterizedPrefix_returnsNull() {
        myFixture.configureByText("test.txt", "block = { \"p_${p("X")}\" <caret>target = 1 }")
        val target = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val prefix = ParadoxMemberService.getKeyPrefix(target)
        Assert.assertNull(prefix)
    }

    // endregion

    // region Injected Root Keys Tests

    @Test
    fun injectRootKeys_affectsGetPath() {
        myFixture.configureByText("test.txt", "a = { <caret>b = 1 }")
        val virtualFile = (myFixture.file as ParadoxScriptFile).virtualFile!!
        val b = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!

        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, listOf("injected"))
        val path = ParadoxMemberService.getPath(b)
        Assert.assertEquals("injected/a/b", path!!.path)
        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_affectsGetRootKeys() {
        myFixture.configureByText("test.txt", "a = { <caret>b = 1 }")
        val virtualFile = (myFixture.file as ParadoxScriptFile).virtualFile!!
        val b = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!

        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, listOf("i1", "i2"))
        val rootKeys = ParadoxMemberService.getRootKeys(b)
        Assert.assertEquals(listOf("i1", "i2", "a"), rootKeys)
        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_doesNotAffectLimitedPath() {
        myFixture.configureByText("test.txt", "a = { b = { <caret>c = 1 } }")
        val virtualFile = (myFixture.file as ParadoxScriptFile).virtualFile!!
        val c = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!

        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, listOf("injected"))
        val limited = ParadoxMemberService.getPath(c, limit = 2)
        Assert.assertEquals("b/c", limited!!.path)
        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_multipleInjected_allPrepended() {
        myFixture.configureByText("test.txt", "root = { <caret>child = 1 }")
        val virtualFile = (myFixture.file as ParadoxScriptFile).virtualFile!!
        val child = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!

        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, listOf("i1", "i2", "i3"))
        val path = ParadoxMemberService.getPath(child)
        Assert.assertEquals("i1/i2/i3/root/child", path!!.path)
        ParadoxAnalysisInjectionManager.injectRootKeys(virtualFile, emptyList())
    }

    // endregion

    // region Edge Cases and Special Values

    @Test
    fun getPath_scriptedVariable_returnsEmptyPath() {
        myFixture.configureByText("test.txt", "<caret>@my_var = 42")
        val variable = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptScriptedVariable>()!!
        val path = ParadoxMemberService.getPath(variable)
        Assert.assertEquals("", path!!.path)
    }

    @Test
    fun getPath_scriptedVariable_nested_returnsContainerPath() {
        myFixture.configureByText("test.txt", "k = { <caret>@my_var = 42 }")
        val variable = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptScriptedVariable>()!!
        val path = ParadoxMemberService.getPath(variable)
        Assert.assertEquals("k", path!!.path)
    }

    @Test
    fun getPath_colorValue_correctPath() {
        myFixture.configureByText("test.txt", """
            settings = {
                <caret>color = rgb { 255 128 64 }
            }
        """)
        val color = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(color)
        Assert.assertEquals("settings/color", path!!.path)
    }

    @Test
    fun getPath_booleanValue_correctPath() {
        myFixture.configureByText("test.txt", """
            config = {
                <caret>enabled = yes
                disabled = no
            }
        """)
        val enabled = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        Assert.assertEquals("config/enabled", ParadoxMemberService.getPath(enabled)!!.path)

        myFixture.configureByText("test.txt", """
            config = {
                enabled = yes
                <caret>disabled = no
            }
        """)
        val disabled = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        Assert.assertEquals("config/disabled", ParadoxMemberService.getPath(disabled)!!.path)
    }

    @Test
    fun getPath_numericValues_correctPath() {
        myFixture.configureByText("test.txt", """
            numbers = {
                <caret>int_val = 42
                float_val = 3.14
                negative = -10
            }
        """)
        Assert.assertEquals("numbers/int_val", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            numbers = {
                int_val = 42
                <caret>float_val = 3.14
                negative = -10
            }
        """)
        Assert.assertEquals("numbers/float_val", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            numbers = {
                int_val = 42
                float_val = 3.14
                <caret>negative = -10
            }
        """)
        Assert.assertEquals("numbers/negative", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)
    }

    @Test
    fun getPath_scriptedVariableReference_correctPath() {
        myFixture.configureByText("test.txt", """
            @cost = 100
            item = {
                <caret>price = @cost
            }
        """)
        val price = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(price)
        Assert.assertEquals("item/price", path!!.path)
    }

    @Test
    fun getPath_inlineMath_correctPath() {
        myFixture.configureByText("test.txt", """
            calc = {
                <caret>result = @[ 1 + 2 * 3 ]
            }
        """)
        val result = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(result)
        Assert.assertEquals("calc/result", path!!.path)
    }

    @Test
    fun getPath_emptyBlock_propertyStillHasPath() {
        myFixture.configureByText("test.txt", """
            <caret>empty = { }
        """)
        val empty = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(empty)
        Assert.assertEquals("empty", path!!.path)
    }

    @Test
    fun getPath_deeplyNested_correctPath() {
        myFixture.configureByText("test.txt", """
            l1 = { l2 = { l3 = { l4 = { l5 = { l6 = { l7 = { l8 = { l9 = { <caret>l10 = 1 } } } } } } } } }
        """)
        val l10 = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        val path = ParadoxMemberService.getPath(l10)
        Assert.assertEquals("l1/l2/l3/l4/l5/l6/l7/l8/l9/l10", path!!.path)
        Assert.assertEquals(10, path.length)
    }

    @Test
    fun getPath_siblingProperties_differentPaths() {
        myFixture.configureByText("test.txt", """
            parent = {
                <caret>child1 = 1
                child2 = 2
                child3 = 3
            }
        """)
        Assert.assertEquals("parent/child1", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            parent = {
                child1 = 1
                <caret>child2 = 2
                child3 = 3
            }
        """)
        Assert.assertEquals("parent/child2", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            parent = {
                child1 = 1
                child2 = 2
                <caret>child3 = 3
            }
        """)
        Assert.assertEquals("parent/child3", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)
    }

    @Test
    fun getPath_multipleRootProperties_eachHasOwnPath() {
        myFixture.configureByText("test.txt", """
            <caret>root1 = 1
            root2 = 2
            root3 = { nested = 1 }
        """)
        Assert.assertEquals("root1", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            root1 = 1
            <caret>root2 = 2
            root3 = { nested = 1 }
        """)
        Assert.assertEquals("root2", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            root1 = 1
            root2 = 2
            <caret>root3 = { nested = 1 }
        """)
        Assert.assertEquals("root3", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            root1 = 1
            root2 = 2
            root3 = { <caret>nested = 1 }
        """)
        Assert.assertEquals("root3/nested", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)
    }

    @Test
    fun getPath_mixedBlockContent_correctPaths() {
        myFixture.configureByText("test.txt", """
            mixed = {
                <caret>prop1 = a
                "value1"
                prop2 = b
                "value2"
            }
        """)
        Assert.assertEquals("mixed/prop1", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            mixed = {
                prop1 = a
                "value1"
                <caret>prop2 = b
                "value2"
            }
        """)
        Assert.assertEquals("mixed/prop2", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptProperty>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            mixed = {
                prop1 = a
                <caret>"value1"
                prop2 = b
                "value2"
            }
        """)
        Assert.assertEquals("mixed/-", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        )!!.path)

        myFixture.configureByText("test.txt", """
            mixed = {
                prop1 = a
                "value1"
                prop2 = b
                <caret>"value2"
            }
        """)
        Assert.assertEquals("mixed/-", ParadoxMemberService.getPath(
            myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptValue>()!!
        )!!.path)
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
        Assert.assertEquals("", path.get(3))
    }

    // endregion
}
