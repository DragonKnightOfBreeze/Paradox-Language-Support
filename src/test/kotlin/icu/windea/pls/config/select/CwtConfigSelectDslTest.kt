package icu.windea.pls.config.select

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.config.members
import icu.windea.pls.config.properties
import icu.windea.pls.config.values
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigSelectDslTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun byPath_simple() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k4 = selectScope { fileConfig.ofPath("k1/k2/k3/k4").one() }
        Assert.assertNotNull(k4)
        val k4List = selectScope { fileConfig.ofPath("k1/k2/k3/k4").all() }
        Assert.assertEquals(3, k4List.size)
    }

    @Test
    fun walkUp_propertyChain() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k4 = selectScope {
            fileConfig.ofPath("k1/k2/k3/k4").asProperty().one()
        }!!

        val keys = selectScope {
            k4.walkUp().asProperty().map { it.key }.toList()
        }
        Assert.assertEquals(listOf("k4", "k3", "k2", "k1"), keys)
    }

    @Test
    fun walkDown_containsAllDescendants() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k1 = selectScope { fileConfig.ofPath("k1").asProperty().one() }!!

        val descendants = selectScope { k1.walkDown().toList() }
        Assert.assertEquals(10, descendants.size)

        val k3Count = descendants.count { it is icu.windea.pls.config.config.CwtPropertyConfig && it.key == "k3" }
        val k4Count = descendants.count { it is icu.windea.pls.config.config.CwtPropertyConfig && it.key == "k4" }
        Assert.assertEquals(3, k3Count)
        Assert.assertEquals(3, k4Count)
    }

    @Test
    fun members_properties_values_basic() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k1 = selectScope { fileConfig.ofPath("k1").asProperty().one() }!!

        val members = selectScope { k1.members().all() }
        Assert.assertEquals(3, members.size)

        val values = selectScope { k1.values().asValue().all() }
        Assert.assertEquals(2, values.size)
        Assert.assertEquals(listOf("v1", "v1"), values.map { it.value })

        val properties = selectScope { k1.properties().all() }
        Assert.assertEquals(1, properties.size)
        Assert.assertEquals("k2", properties.single().key)
    }

    @Test
    fun asBlock_and_dashPathSegment() {
        val fileConfig = resolveFileConfig("features/select/select_blocks.test.cwt")
        val block = selectScope { fileConfig.values().asBlock().one() }
        Assert.assertNotNull(block)

        val a = selectScope { fileConfig.ofPath("-/a").asProperty().one() }
        Assert.assertNotNull(a)
        Assert.assertEquals("a", a!!.key)
    }

    @Test
    fun asBlock_emptyResult() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val block = selectScope { fileConfig.values().asBlock().one() }
        Assert.assertNull(block)
    }

    @Test
    fun ofKey_ignoreCase_and_usePattern() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k2 = selectScope { fileConfig.ofPath("k1/k2").asProperty().one() }!!

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
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k2 = selectScope { fileConfig.ofPath("k1/k2").asProperty().one() }!!

        val k3List = selectScope {
            k2.properties().ofKeys(listOf("k3", "not_exists"), ignoreCase = true, usePattern = false).all()
        }
        Assert.assertEquals(3, k3List.size)
    }

    @Test
    fun ofPath_usePattern_and_emptyPath() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")

        val k4ListByPattern = selectScope {
            fileConfig.ofPath("k1/*/k3/k4", ignoreCase = false, usePattern = true).asProperty().all()
        }
        Assert.assertEquals(3, k4ListByPattern.size)
        Assert.assertTrue(k4ListByPattern.all { it.key == "k4" })

        val k4ListByLiteral = selectScope {
            fileConfig.ofPath("k1/*/k3/k4", ignoreCase = false, usePattern = false).all()
        }
        Assert.assertEquals(0, k4ListByLiteral.size)

        val empty = selectScope { fileConfig.ofPath("").all() }
        Assert.assertTrue(empty.isEmpty())
    }

    @Test
    fun ofPaths_basic() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val list = selectScope {
            fileConfig.ofPaths(listOf("k1/k2/k3/k4", "k1/k2/k3")).asProperty().all()
        }
        val k4Count = list.count { it.key == "k4" }
        val k3Count = list.count { it.key == "k3" }
        Assert.assertEquals(3, k4Count)
        Assert.assertEquals(3, k3Count)
    }

    @Test
    fun ofPath_duplicateKeyOrderIsStable() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k4List = selectScope { fileConfig.ofPath("k1/k2/k3/k4").asProperty().all() }
        Assert.assertEquals(3, k4List.size)

        val k3Paths = selectScope {
            k4List.mapNotNull { it.parentConfig.asProperty()?.key }
        }
        Assert.assertEquals(listOf("k3", "k3", "k3"), k3Paths)
    }

    @Test
    fun ofPath_usePattern_notMatchedShouldBeEmpty() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val result = selectScope {
            fileConfig.ofPath("k1/*/kx/k4", ignoreCase = false, usePattern = true).all()
        }
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun ofPaths_shouldIgnoreEmptyOrInvalidPath() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val list = selectScope {
            fileConfig.ofPaths(listOf("", "not_exists", "k1/k2/k3/k4"), ignoreCase = false, usePattern = false)
                .asProperty()
                .all()
        }
        Assert.assertEquals(3, list.size)
        Assert.assertTrue(list.all { it.key == "k4" })
    }

    @Test
    fun sequenceOfContainers_ofPath_and_ofPaths() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k1 = selectScope { fileConfig.ofPath("k1").asProperty().one() }!!
        val k2 = selectScope { fileConfig.ofPath("k1/k2").asProperty().one() }!!

        val k4FromContainers = selectScope {
            sequenceOf(k1, k2).ofPath("k3/k4").asProperty().all()
        }
        Assert.assertEquals(3, k4FromContainers.size)

        val listFromContainers = selectScope {
            sequenceOf(k1, k2).ofPaths(listOf("k3", "k3/k4")).asProperty().all()
        }
        Assert.assertEquals(6, listFromContainers.size)
    }

    @Test
    fun walkDown_withTraversalParameter() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k1 = selectScope { fileConfig.ofPath("k1").asProperty().one() }!!

        val preOrder = selectScope { k1.walkDown(TreeTraversal.PRE_ORDER_DFS).toList() }
        val postOrder = selectScope { k1.walkDown(TreeTraversal.POST_ORDER_DFS).toList() }
        Assert.assertEquals(preOrder.size, postOrder.size)
        Assert.assertNotEquals(preOrder.map { it.toString() }, postOrder.map { it.toString() })
    }

    @Test
    fun ofValue_and_ofValues_valueNormalization() {
        val fileConfig = resolveFileConfig("features/select/select_values.test.cwt")

        val stringProp = selectScope { fileConfig.properties().ofKey("string_value").one() }!!
        val blockProp = selectScope { fileConfig.properties().ofKey("block_value").one() }!!
        val numberProp = selectScope { fileConfig.properties().ofKey("number_value").one() }!!
        val booleanProp = selectScope { fileConfig.properties().ofKey("boolean_value").one() }!!

        Assert.assertEquals("Text", stringProp.value)
        Assert.assertEquals("{...}", blockProp.value)
        Assert.assertEquals("1.0", numberProp.value)
        Assert.assertEquals("yes", booleanProp.value)

        val vText = selectScope { fileConfig.properties().ofValue("text", ignoreCase = true).one() }
        Assert.assertNotNull(vText)
        val vTextSensitive = selectScope { fileConfig.properties().ofValue("text", ignoreCase = false).one() }
        Assert.assertNull(vTextSensitive)

        val list = selectScope {
            fileConfig.properties().ofValues(listOf("Text", "{...}", "1.0", "yes"), ignoreCase = true).all()
        }
        Assert.assertEquals(4, list.size)
    }

    private fun resolveFileConfig(path: String): CwtFileConfig {
        myFixture.configureByFile(path)
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val fileConfig = CwtFileConfig.resolve(file, configGroup, file.name)
        return fileConfig
    }
}
