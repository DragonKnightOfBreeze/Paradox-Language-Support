package icu.windea.pls.lang.select

import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.values
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.isDirectValue
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxPsiSelectDslTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureScriptFile(@TestDataFile testDataPath: String): ParadoxScriptFile {
        myFixture.configureByFile(testDataPath)
        return myFixture.file as ParadoxScriptFile
    }

    @Test
    fun byPath_simple() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k4 = selectScope { file.queryBy("k1/k2/k3/k4").one() }
        Assert.assertNotNull(k4)
        val k4List = selectScope { file.queryBy("k1/k2/k3/k4").asProperty().all() }
        Assert.assertEquals(3, k4List.size)
    }

    @Test
    fun walkDown_containsAllDescendants() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k1 = selectScope { file.queryBy("k1").asProperty().one() }!!

        val descendants = selectScope { k1.walkDown().toList() }
        Assert.assertTrue(descendants.size >= 10)

        val k3Count = descendants.count { it is ParadoxScriptProperty && it.name == "k3" }
        val k4Count = descendants.count { it is ParadoxScriptProperty && it.name == "k4" }
        Assert.assertEquals(3, k3Count)
        Assert.assertEquals(3, k4Count)
    }

    @Test
    fun members_properties_values_basic() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k1 = selectScope { file.queryBy("k1").asProperty().one() }!!

        val members = selectScope { k1.members().all() }
        Assert.assertEquals(3, members.size)

        val values = selectScope { k1.values().asValue().all() }
        Assert.assertEquals(2, values.size)
        Assert.assertEquals(listOf("v1", "v1"), values.map { it.value })

        val properties = selectScope { k1.properties().all() }
        Assert.assertEquals(1, properties.size)
        Assert.assertEquals("k2", properties.single().name)
    }

    @Test
    fun asBlock_and_dashPathSegment() {
        val file = configureScriptFile("features/select/select_blocks.test.txt")
        val block = selectScope { file.queryBy("list/-").asBlock().one() }
        Assert.assertNotNull(block)

        val a = selectScope { file.queryBy("list/-/a").asProperty().one() }
        Assert.assertNotNull(a)
        Assert.assertEquals("a", a!!.name)
    }

    @Test
    fun asBlock_emptyResult() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val block = selectScope { file.values().asBlock().one() }
        Assert.assertNull(block)
    }

    @Test
    fun ofKey_ignoreCase_and_usePattern() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k2 = selectScope { file.queryBy("k1/k2").asProperty().one() }!!

        val k3IgnoreCase = selectScope {
            k2.properties().ofKey("K3", ignoreCase = true, usePattern = false).all()
        }
        Assert.assertEquals(3, k3IgnoreCase.size)

        val k3CaseSensitive = selectScope {
            k2.properties().ofKey("K3", ignoreCase = false, usePattern = false).all()
        }
        Assert.assertEquals(0, k3CaseSensitive.size)

        val k3ByPattern = selectScope {
            k2.properties().ofKey("k*", ignoreCase = false, usePattern = true).all()
        }
        Assert.assertEquals(3, k3ByPattern.size)
    }

    @Test
    fun ofKeys_basic() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k2 = selectScope { file.queryBy("k1/k2").asProperty().one() }!!

        val k3List = selectScope {
            k2.properties().ofKeys(listOf("k3", "not_exists"), ignoreCase = true, usePattern = false).all()
        }
        Assert.assertEquals(3, k3List.size)
    }

    @Test
    fun queryBy_usePattern_and_emptyPath() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")

        val k4ListByPattern = selectScope {
            file.queryBy("k1/*/k3/k4", ignoreCase = false, usePattern = true).asProperty().all()
        }
        Assert.assertEquals(3, k4ListByPattern.size)
        Assert.assertTrue(k4ListByPattern.all { it.name == "k4" })

        val k4ListByLiteral = selectScope {
            file.queryBy("k1/*/k3/k4", ignoreCase = false, usePattern = false).all()
        }
        Assert.assertEquals(0, k4ListByLiteral.size)

        val empty = selectScope { file.queryBy("").all() }
        Assert.assertTrue(empty.isEmpty())
    }

    @Test
    fun queryBy_basic() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val list = selectScope {
            file.queryBy(listOf("k1/k2/k3/k4", "k1/k2/k3")).asProperty().all()
        }
        val k4Count = list.count { it.name == "k4" }
        val k3Count = list.count { it.name == "k3" }
        Assert.assertEquals(3, k4Count)
        Assert.assertEquals(3, k3Count)
    }

    @Test
    fun queryBy_duplicateKeyOrderIsStable() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k4List = selectScope { file.queryBy("k1/k2/k3/k4").asProperty().all() }
        Assert.assertEquals(3, k4List.size)

        val k3Paths = selectScope {
            k4List.mapNotNull { it.parentOfType<ParadoxScriptProperty>(withSelf = false)?.name }
        }
        Assert.assertEquals(listOf("k3", "k3", "k3"), k3Paths)
    }

    @Test
    fun queryBy_usePattern_notMatchedShouldBeEmpty() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val result = selectScope {
            file.queryBy("k1/*/kx/k4", ignoreCase = false, usePattern = true).all()
        }
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun queryBy_shouldIgnoreEmptyOrInvalidPath() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val list = selectScope {
            file.queryBy(listOf("", "not_exists", "k1/k2/k3/k4"), ignoreCase = false, usePattern = false)
                .asProperty()
                .all()
        }
        Assert.assertEquals(3, list.size)
        Assert.assertTrue(list.all { it.name == "k4" })
    }

    @Test
    fun sequenceOfContainers_queryBy_and_queryBy() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k1 = selectScope { file.queryBy("k1").asProperty().one() }!!
        val k2 = selectScope { file.queryBy("k1/k2").asProperty().one() }!!

        val k4FromContainers = selectScope {
            sequenceOf(k1, k2).queryBy("k3/k4").asProperty().all()
        }
        Assert.assertEquals(3, k4FromContainers.size)

        val listFromContainers = selectScope {
            sequenceOf(k1, k2).queryBy(listOf("k3", "k3/k4")).asProperty().all()
        }
        Assert.assertEquals(6, listFromContainers.size)
    }

    @Test
    fun walkDown_withTraversalParameter() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k1 = selectScope { file.queryBy("k1").asProperty().one() }!!

        val preOrder = selectScope { k1.walkDown(TreeTraversal.PRE_ORDER_DFS).toList() }
        val postOrder = selectScope { k1.walkDown(TreeTraversal.POST_ORDER_DFS).toList() }
        Assert.assertEquals(preOrder.size, postOrder.size)
        Assert.assertNotEquals(preOrder.map { it.startOffset }, postOrder.map { it.startOffset })
    }

    @Test
    fun ofValue_and_ofValues_valueNormalization() {
        val file = configureScriptFile("features/select/select_values.test.txt")

        val stringProp = selectScope { file.properties().ofKey("string_value").one() }!!
        val blockProp = selectScope { file.properties().ofKey("block_value").one() }!!
        val numberProp = selectScope { file.properties().ofKey("number_value").one() }!!
        val booleanProp = selectScope { file.properties().ofKey("boolean_value").one() }!!

        Assert.assertEquals("Text", stringProp.value)
        Assert.assertEquals("{...}", blockProp.value)
        Assert.assertEquals("1.0", numberProp.value)
        Assert.assertEquals("yes", booleanProp.value)

        val vText = selectScope { file.properties().ofValue("text", ignoreCase = true).one() }
        Assert.assertNotNull(vText)
        val vTextSensitive = selectScope { file.properties().ofValue("text", ignoreCase = false).one() }
        Assert.assertNull(vTextSensitive)

        val list1 = selectScope { file.properties().ofValues(listOf("Text", "1.0", "yes"), ignoreCase = true).all() }
        Assert.assertEquals(3, list1.size) // include properties with literal values

        val list2 = selectScope { file.properties().ofValues(listOf("Text", "1.0", "yes", "{...}"), ignoreCase = true).all() }
        Assert.assertEquals(3, list2.size) // exclude properties with block-like values
    }

    @Test
    fun conditional_true_and_false() {
        val file = configureScriptFile("features/select/select_conditional.test.txt")
        val settings = selectScope { file.queryBy("settings").asProperty().one() }!!

        val membersConditionalFalse = selectScope { settings.members(conditional = false).all() }
        Assert.assertEquals(2, membersConditionalFalse.size)
        Assert.assertTrue(membersConditionalFalse.none { it is ParadoxScriptProperty && it.name == "conditional_block" })

        val membersConditionalTrue = selectScope { settings.members(conditional = true).all() }
        Assert.assertEquals(3, membersConditionalTrue.size)
        Assert.assertTrue(membersConditionalTrue.any { it is ParadoxScriptProperty && it.name == "conditional_block" })
    }

    @Test
    fun walkUp_propertyChain() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")
        val k4 = selectScope { file.queryBy("k1/k2/k3/k4").asProperty().one() }!!

        val keys = selectScope {
            k4.walkUp().asProperty().map { it.name }.toList()
        }
        Assert.assertEquals(listOf("k4", "k3", "k2", "k1"), keys)
    }

    @Test
    fun queryParentBy_basic() {
        val file = configureScriptFile("features/select/select_test_1.test.txt")

        val k4 = selectScope { file.queryBy("k1/k2/k3/k4").asProperty().one() }!!
        Assert.assertEquals("k4", k4.name)

        val empty = selectScope { k4.queryParentBy("") }
        Assert.assertNull(empty)

        val k4WithSelf = selectScope { k4.queryParentBy("k4").asProperty() }
        Assert.assertEquals("k4", k4WithSelf?.name)

        val k3WithSelf = selectScope { k4.queryParentBy("k3/k4").asProperty() }
        Assert.assertEquals("k3", k3WithSelf?.name)

        val k3 = selectScope { k4.queryParentBy("k3", withSelf = false).asProperty() }
        Assert.assertEquals("k3", k3?.name)

        val k2AnyWithSelf = selectScope { k4.queryParentBy("*/k3/k4").asProperty() }
        Assert.assertEquals("k2", k2AnyWithSelf?.name)

        val v2 = selectScope { file.queryBy("k1/k2/-").asValue().one() }!!
        Assert.assertEquals("v2", v2.value)

        val k2FromV2WithSelf = selectScope { v2.queryParentBy("k2/-").asProperty() }
        Assert.assertEquals("k2", k2FromV2WithSelf?.name)

        val invalidWithSelf = selectScope { k4.queryParentBy("not_exists") }
        Assert.assertNull(invalidWithSelf)

        val invalid = selectScope { k4.queryParentBy("not_exists", withSelf = true) }
        Assert.assertNull(invalid)
    }

    @Test
    fun queryParentBy_directBlock() {
        val file2 = configureScriptFile("features/select/select_blocks.test.txt")

        val directBlock = selectScope { file2.queryBy("list/-").asBlock().one() }!!
        Assert.assertTrue(directBlock.isDirectValue())

        val a = selectScope { file2.queryBy("list/-/a").asProperty().one() }!!
        Assert.assertEquals("a", a.name)

        val directBlockFromA = selectScope { a.queryParentBy("-", withSelf = false).asBlock() }!!
        Assert.assertTrue(directBlockFromA.isDirectValue())

        val directBlockFromAWithSelf = selectScope { a.queryParentBy("-/a").asBlock() }!!
        Assert.assertTrue(directBlockFromAWithSelf.isDirectValue())

        val listFromDirectBlock = selectScope { directBlockFromAWithSelf.queryParentBy("list", withSelf = false).asProperty() }!!
        Assert.assertEquals("list", listFromDirectBlock.name)

        val listFromDirectBlockWithSelf = selectScope { directBlockFromAWithSelf.queryParentBy("list/-").asProperty() }!!
        Assert.assertEquals("list", listFromDirectBlockWithSelf.name)

        val invalidWithSelf = selectScope { a.queryParentBy("list") }
        Assert.assertNull(invalidWithSelf)

        val invalidCrossingDirectBlock = selectScope { a.queryParentBy("list", withSelf = false) }
        Assert.assertNull(invalidCrossingDirectBlock)
    }
}
