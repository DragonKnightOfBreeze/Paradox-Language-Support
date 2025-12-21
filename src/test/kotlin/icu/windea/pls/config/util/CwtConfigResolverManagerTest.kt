package icu.windea.pls.config.util

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigResolverManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testFindConfigsByPathExpression_basic_properties() {
        myFixture.configureByFile("features/config/resolver_find_configs_by_path_expression.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        val filePath = "common/test/resolver_find_configs_by_path_expression.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)
        CwtConfigResolverManager.getFileConfigs(configGroup)[filePath] = fileConfig

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@match_a")
            assertNotNull(result)
            assertEquals(1, result!!.size)
            val p = result.single() as CwtPropertyConfig
            assertEquals("match_a", p.key)
            assertEquals("1", p.value)
            assertNull(p.parentConfig)
            assertNotNull(p.pointer.element)
            assertSame(file, p.pointer.element!!.containingFile)
        }

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@match_*")
            assertNotNull(result)
            val keys = result!!.filterIsInstance<CwtPropertyConfig>().map { it.key }.sorted()
            assertEquals(listOf("match_a", "match_b"), keys)
            result.filterIsInstance<CwtPropertyConfig>().forEach {
                assertNull(it.parentConfig)
                assertNotNull(it.pointer.element)
                assertSame(file, it.pointer.element!!.containingFile)
            }
        }

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@container/child/grand")
            assertNotNull(result)
            assertEquals(1, result!!.size)
            val p = result.single() as CwtPropertyConfig
            assertEquals("grand", p.key)
            assertEquals("1", p.value)

            val parent = p.parentConfig
            assertNotNull(parent)
            assertTrue(parent is CwtPropertyConfig)
            assertEquals("child", (parent as CwtPropertyConfig).key)
            assertNotNull(p.pointer.element)
            assertSame(file, p.pointer.element!!.containingFile)
        }
    }

    @Test
    fun testFindConfigsByPathExpression_basic_values() {
        myFixture.configureByFile("features/config/resolver_find_configs_by_path_expression.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        val filePath = "common/test/resolver_find_configs_by_path_expression.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)
        CwtConfigResolverManager.getFileConfigs(configGroup)[filePath] = fileConfig

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@-")
            assertNotNull(result)
            val values = result!!.filterIsInstance<CwtValueConfig>().map { it.value }.sorted()
            assertEquals(listOf("top_value1", "top_value2"), values)
            result.filterIsInstance<CwtValueConfig>().forEach {
                assertNull(it.parentConfig)
                assertNotNull(it.pointer.element)
                assertSame(file, it.pointer.element!!.containingFile)
            }
        }

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@container/values_holder/-")
            assertNotNull(result)
            val values = result!!.filterIsInstance<CwtValueConfig>().map { it.value }.sorted()
            assertEquals(listOf("inner_value1", "inner_value2"), values)

            val first = result.filterIsInstance<CwtValueConfig>().first()
            val parent = first.parentConfig
            assertNotNull(parent)
            assertTrue(parent is CwtPropertyConfig)
            assertEquals("values_holder", (parent as CwtPropertyConfig).key)
            assertNotNull(first.pointer.element)
            assertSame(file, first.pointer.element!!.containingFile)
        }
    }

    @Test
    fun testFindConfigsByPathExpression_boundaries() {
        myFixture.configureByFile("features/config/resolver_find_configs_by_path_expression.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        val filePath = "common/test/resolver_find_configs_by_path_expression.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)
        CwtConfigResolverManager.getFileConfigs(configGroup)[filePath] = fileConfig

        // ignore case
        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@mixedcase")
            assertNotNull(result)
            assertEquals(1, result!!.size)
            val p = result.single() as CwtPropertyConfig
            assertEquals("MixedCase", p.key)
            assertEquals("3", p.value)
        }

        // use '-' then go deeper into value block
        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(
                configGroup,
                "$filePath@container2/values_block/-/block_value/x"
            )
            assertNotNull(result)
            assertEquals(1, result!!.size)
            val p = result.single() as CwtPropertyConfig
            assertEquals("x", p.key)
            assertEquals("1", p.value)

            val p1 = p.parentConfig
            assertNotNull(p1)
            assertTrue(p1 is CwtPropertyConfig)
            val p1p = p1 as CwtPropertyConfig
            assertEquals("block_value", p1p.key)
            val p2 = p1p.parentConfig
            assertNotNull(p2)
            assertTrue(p2 is CwtValueConfig)
            val p2v = p2 as CwtValueConfig
            val p3 = p2v.parentConfig
            assertNotNull(p3)
            assertTrue(p3 is CwtPropertyConfig)
            assertEquals("values_block", (p3 as CwtPropertyConfig).key)
            assertNotNull(p.pointer.element)
            assertSame(file, p.pointer.element!!.containingFile)
        }

        // short-circuit to empty
        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@container/not_exists")
            assertNotNull(result)
            assertTrue(result!!.isEmpty())
        }

        // empty rule path should return empty list
        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "$filePath@")
            assertNotNull(result)
            assertTrue(result!!.isEmpty())
        }
    }

    @Test
    fun testFindConfigsByPathExpression_invalid_or_not_found() {
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "invalid")
            assertNull(result)
        }

        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "common/test/not_exists.cwt@match_a")
            assertNull(result)
        }

        // file path is exact match
        run {
            val result = CwtConfigResolverManager.findConfigsByPathExpression(configGroup, "COMMON/TEST/NOT_EXISTS.CWT@match_a")
            assertNull(result)
        }
    }
}
