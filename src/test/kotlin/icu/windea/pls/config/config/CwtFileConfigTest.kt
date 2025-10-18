package icu.windea.pls.config.config

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile
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
        run {
            val p = props.getValue("prop_float")
            assertEquals("1.5", p.value)
            assertEquals(CwtType.Float, p.valueType)
            assertNull(p.configs)
            assertEquals(CwtType.Float, p.valueConfig!!.valueType)
        }
        run {
            val p = props.getValue("prop_string")
            // value removes quotes
            assertEquals("abc", p.value)
            assertEquals(CwtType.String, p.valueType)
        }
        run {
            val p = props.getValue("prop_ident")
            // bare identifier is a String at PSI/type level
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
            // when valueType == Block and no explicit nested members collected, resolver guarantees non-null (possibly empty) list
            assertNotNull(p.configs)
            // from test data it contains 3 members: inner_prop(property), inner_val(value), nested_opt_target(property)
            assertEquals(3, p.configs!!.size)

            // valueConfig is backed by property config (wrapper)
            val v = p.valueConfig
            assertNotNull(v)
            assertSame(p, v!!.propertyConfig)
            assertEquals(CwtType.Block, v.valueType)
            assertNotNull(v.configs)
            assertEquals(p.configs!!.size, v.configs!!.size)

            // option configs attached before prop_block
            val opts = p.optionConfigs
            assertNotNull(opts)
            // required (value only) + severity = warning
            assertEquals(2, opts!!.size)
            val hasRequired = opts.any { it is CwtOptionValueConfig && it.value == "required" }
            val hasSeverity = opts.any { it is CwtOptionConfig && it.key == "severity" && it.value == "warning" }
            assertTrue(hasRequired)
            assertTrue(hasSeverity)
        }

        // top-level values
        val values = fileConfig.values
        // we expect 2: top_value1 and "top quoted"
        assertEquals(2, values.size)

        val vMap = values.groupBy { it.value }
        run {
            val v = vMap.getValue("top_value1").single()
            assertEquals(CwtType.String, v.valueType)
            // option attached before top_value1
            val opts = v.optionConfigs
            assertNotNull(opts)
            val hasTag = opts!!.any { it is CwtOptionValueConfig && it.value == "tag" }
            assertTrue(hasTag)
        }
        run {
            val v = vMap.getValue("top quoted").single()
            assertEquals(CwtType.String, v.valueType)
        }
    }
}
