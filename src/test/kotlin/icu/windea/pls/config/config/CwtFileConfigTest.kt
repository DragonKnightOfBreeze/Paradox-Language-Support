package icu.windea.pls.config.config

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtFileConfigTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testResolve_file_basic() {
        myFixture.configureByFile("features/config/file_config_basic.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroup(project, ParadoxGameType.Stellaris)

        val filePath = "common/test/file_config_basic.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)

        // file level
        assertEquals(file.name, fileConfig.name)
        assertEquals(filePath, fileConfig.path)

        // properties mapping
        val props = fileConfig.properties.associateBy { it.key }
        assertTrue(props.containsKey("prop_int"))
        assertTrue(props.containsKey("prop_float"))
        assertTrue(props.containsKey("prop_string"))
        assertTrue(props.containsKey("prop_ident"))
        assertTrue(props.containsKey("prop_bool_yes"))
        assertTrue(props.containsKey("prop_bool_no"))
        assertTrue(props.containsKey("prop_block"))

        run {
            val p = props.getValue("prop_int")
            assertEquals("1", p.value)
            assertEquals(CwtType.Int, p.valueType)
            assertNull(p.configs) // non-block => no nested member list
            assertNotNull(p.valueConfig)
            assertEquals(CwtType.Int, p.valueConfig!!.valueType)
        }
    }

    @Test
    fun testResolve_file_boundaries() {
        myFixture.configureByFile("features/config/file_config_boundaries.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroup(project, ParadoxGameType.Stellaris)

        val filePath = "common/test/file_config_boundaries.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)

        val props = fileConfig.properties.associateBy { it.key }

        // label option with space should be picked even with normal comment between
        run {
            val p = props.getValue("space_prop")
            val opts = p.optionConfigs
            assertNotNull(opts)
            val hasLabel = opts.any { it is CwtOptionValueConfig && it.value == "label with space" }
            assertTrue(hasLabel)
        }

        // multiple options with different separators
        run {
            val p = props.getValue("mode_prop")
            val opts = p.optionConfigs.filterIsInstance<CwtOptionConfig>()
            assertEquals(2, opts.size)
            val eq = opts.any { it.key == "mode" && it.separatorType.name == CwtSeparatorType.EQUAL.name && it.value == "strict" }
            val ne = opts.any { it.key == "mode" && it.separatorType.name == CwtSeparatorType.NOT_EQUAL.name && it.value == "relax" }
            assertTrue(eq)
            assertTrue(ne)
        }

        // option with block value, containing nested option members
        run {
            val p = props.getValue("opt_block_prop")
            val meta = p.optionConfigs.filterIsInstance<CwtOptionConfig>().single { it.key == "meta" }
            assertEquals(CwtType.Block, meta.valueType)
            val nested = meta.optionConfigs
            assertNotNull(nested)
            val inner = nested!!.filterIsInstance<CwtOptionConfig>().any { it.key == "inner" && it.value == "1" }
            val foo = nested.any { it is CwtOptionValueConfig && it.value == "inner_val" }
            assertTrue(inner)
            assertTrue(foo)
        }

        // empty block property -> configs should be non-null and empty for both property and its valueConfig
        run {
            val p = props.getValue("empty_block_prop")
            assertEquals(CwtType.Block, p.valueType)
            assertNotNull(p.configs)
            assertTrue(p.configs!!.isEmpty())
            val v = p.valueConfig
            assertNotNull(v)
            assertNotNull(v!!.configs)
            assertTrue(v.configs!!.isEmpty())
        }

        // number formats
        assertEquals(CwtType.Float, props.getValue("prop_float_no_leading_zero").valueType)
        assertEquals(".5", props.getValue("prop_float_no_leading_zero").value)
        assertEquals(CwtType.Int, props.getValue("prop_int_leading_zero").valueType)
        assertEquals("007", props.getValue("prop_int_leading_zero").value)
        assertEquals(CwtType.Int, props.getValue("prop_int_negative").valueType)
        assertEquals("-3", props.getValue("prop_int_negative").value)
        assertEquals(CwtType.Float, props.getValue("prop_float_negative").valueType)
        assertEquals("-0.75", props.getValue("prop_float_negative").value)
    }
    @Test
    fun testResolve_file_basic_rest() {
        myFixture.configureByFile("features/config/file_config_basic.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroup(project, ParadoxGameType.Stellaris)

        val filePath = "common/test/file_config_basic.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)

        val props = fileConfig.properties.associateBy { it.key }

        run {
            val p = props.getValue("prop_float")
            assertEquals("1.5", p.value)
            assertEquals(CwtType.Float, p.valueType)
            assertNull(p.configs)
            assertEquals(CwtType.Float, p.valueConfig!!.valueType)
        }
        run {
            val p = props.getValue("prop_string")
            assertEquals("abc", p.value)
            assertEquals(CwtType.String, p.valueType)
        }
        run {
            val p = props.getValue("prop_ident")
            assertEquals("int", p.value)
            assertEquals(CwtType.String, p.valueType)
        }
        run {
            val p = props.getValue("prop_bool_yes")
            assertEquals("yes", p.value)
            assertEquals(CwtType.Boolean, p.valueType)
        }
        run {
            val p = props.getValue("prop_bool_no")
            assertEquals("no", p.value)
            assertEquals(CwtType.Boolean, p.valueType)
        }
        run {
            val p = props.getValue("prop_block")
            assertEquals(CwtType.Block, p.valueType)
            assertNotNull(p.configs)
            assertEquals(3, p.configs!!.size)

            val v = p.valueConfig
            assertNotNull(v)
            assertSame(p, v!!.propertyConfig)
            assertEquals(CwtType.Block, v.valueType)
            assertNotNull(v.configs)
            assertEquals(p.configs!!.size, v.configs!!.size)

            val opts = p.optionConfigs
            assertNotNull(opts)
            assertEquals(2, opts.size)
            val hasRequired = opts.any { it is CwtOptionValueConfig && it.value == "required" }
            val hasSeverity = opts.any { it is CwtOptionConfig && it.key == "severity" && it.value == "warning" }
            assertTrue(hasRequired)
            assertTrue(hasSeverity)
        }

        val values = fileConfig.values
        assertEquals(2, values.size)

        val vMap = values.groupBy { it.value }
        run {
            val v = vMap.getValue("top_value1").single()
            assertEquals(CwtType.String, v.valueType)
            val opts = v.optionConfigs
            assertNotNull(opts)
            val hasTag = opts.any { it is CwtOptionValueConfig && it.value == "tag" }
            assertTrue(hasTag)
        }
        run {
            val v = vMap.getValue("top quoted").single()
            assertEquals(CwtType.String, v.valueType)
        }
    }
}
