package icu.windea.pls.config.config
import com.intellij.openapi.util.Key
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.findChild
import icu.windea.pls.core.util.createKey
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtPropertyConfigTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private fun prepare(): Triple<CwtFile, CwtConfigGroup, String> {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroup(project, ParadoxGameType.Stellaris)
        val path = "common/test/property_config_cases.cwt"
        return Triple(file, group, path)
    }

    @Test
    fun testKeyValueTypes_and_Separators() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!

        // quoted key
        val pQuoted = root.findChild<CwtProperty> { it.name == "quoted key" }!!
        val cQuoted = CwtPropertyConfig.resolve(pQuoted, file, group)!!
        assertEquals("quoted key", cQuoted.key)
        assertEquals("1", cQuoted.value)
        assertEquals(CwtType.Int, cQuoted.valueType)
        assertEquals(CwtSeparatorType.EQUAL, cQuoted.separatorType)

        // not equals variants
        val pNe1 = root.findChild<CwtProperty> { it.name == "not_equal1" }!!
        val cNe1 = CwtPropertyConfig.resolve(pNe1, file, group)!!
        assertEquals(CwtSeparatorType.NOT_EQUAL, cNe1.separatorType)
        assertEquals("2", cNe1.value)
        assertEquals(CwtType.Int, cNe1.valueType)

        val pNe2 = root.findChild<CwtProperty> { it.name == "not_equal2" }!!
        val cNe2 = CwtPropertyConfig.resolve(pNe2, file, group)!!
        assertEquals(CwtSeparatorType.NOT_EQUAL, cNe2.separatorType)
        assertEquals("3", cNe2.value)
        assertEquals(CwtType.Int, cNe2.valueType)

        // string value unquotes
        val pStr = root.findChild<CwtProperty> { it.name == "str_prop" }!!
        val cStr = CwtPropertyConfig.resolve(pStr, file, group)!!
        assertEquals("s v", cStr.value)
        assertEquals(CwtType.String, cStr.valueType)
    }

    @Test
    fun testBlockProperty_and_ValueConfig() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!
        val p = root.findChild<CwtProperty> { it.name == "block_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!
        assertEquals(CwtType.Block, c.valueType)
        assertNotNull(c.configs)
        // block has 2 members: a(property) and val1(value)
        assertEquals(2, c.configs!!.size)

        val v = c.valueConfig
        assertNotNull(v)
        assertSame(c, v!!.propertyConfig)
        assertEquals(CwtType.Block, v.valueType)
        assertNotNull(v.configs)
        assertEquals(c.configs!!.size, v.configs!!.size)
    }

    @Test
    fun testOptionConfigs_onProperty() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!
        val p = root.findChild<CwtProperty> { it.name == "opt_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!
        val opts = c.optionConfigs
        assertNotNull(opts)
        // ## required ; ## severity = info
        assertEquals(2, opts!!.size)
        val hasRequired = opts.any { it is CwtOptionValueConfig && it.value == "required" }
        val hasSeverity = opts.any { it is CwtOptionConfig && it.key == "severity" && it.value == "info" }
        assertTrue(hasRequired)
        assertTrue(hasSeverity)
    }

    @Test
    fun testBoundaries_propertyOptions_and_NumberFormats() {
        myFixture.configureByFile("features/config/property_config_boundaries.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroup(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        // option value with space, with normal comment between option and property
        run {
            val p = root.findChild<CwtProperty> { it.name == "space_prop" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            val opts = c.optionConfigs
            assertNotNull(opts)
            assertTrue(opts!!.any { it is CwtOptionValueConfig && it.value == "label with space" })
        }

        // two options with different separators for same key
        run {
            val p = root.findChild<CwtProperty> { it.name == "mode_prop" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            val opts = c.optionConfigs!!.filterIsInstance<CwtOptionConfig>()
            assertEquals(2, opts.size)
            assertTrue(opts.any { it.key == "mode" && it.separatorType == CwtSeparatorType.EQUAL && it.value == "strict" })
            assertTrue(opts.any { it.key == "mode" && it.separatorType == CwtSeparatorType.NOT_EQUAL && it.value == "relax" })
        }

        // option with block value having nested option members
        run {
            val p = root.findChild<CwtProperty> { it.name == "opt_block_prop" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            val meta = c.optionConfigs!!.filterIsInstance<CwtOptionConfig>().single { it.key == "meta" }
            assertEquals(CwtType.Block, meta.valueType)
            val nested = meta.optionConfigs
            assertNotNull(nested)
            assertTrue(nested!!.filterIsInstance<CwtOptionConfig>().any { it.key == "inner" && it.value == "1" })
            assertTrue(nested.any { it is CwtOptionValueConfig && it.value == "inner_val" })
        }

        // empty block property -> configs should be non-null and empty
        run {
            val p = root.findChild<CwtProperty> { it.name == "empty_block_prop" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            assertEquals(CwtType.Block, c.valueType)
            assertNotNull(c.configs)
            assertTrue(c.configs!!.isEmpty())
            val v = c.valueConfig
            assertNotNull(v)
            assertNotNull(v!!.configs)
            assertTrue(v.configs!!.isEmpty())
        }

        // number formats
        run {
            val p = root.findChild<CwtProperty> { it.name == "prop_float_no_leading_zero" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            assertEquals(CwtType.Float, c.valueType)
            assertEquals(".5", c.value)
        }
        run {
            val p = root.findChild<CwtProperty> { it.name == "prop_int_leading_zero" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            assertEquals(CwtType.Int, c.valueType)
            assertEquals("007", c.value)
        }
        run {
            val p = root.findChild<CwtProperty> { it.name == "prop_int_negative" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            assertEquals(CwtType.Int, c.valueType)
            assertEquals("-3", c.value)
        }
        run {
            val p = root.findChild<CwtProperty> { it.name == "prop_float_negative" }!!
            val c = CwtPropertyConfig.resolve(p, file, group)!!
            assertEquals(CwtType.Float, c.valueType)
            assertEquals("-0.75", c.value)
        }
    }

    // region Resolver: create/copy/delegated/delegatedWith + userData

    private val EXTRA_KEY: Key<String> = createKey("test.extra.property")

    @Test
    fun testResolver_create_copy_delegated_forProperty() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!

        // base property (block)
        val baseProp = root.findChild<CwtProperty> { it.name == "block_prop" }!!
        val baseCfg = CwtPropertyConfig.resolve(baseProp, file, group)!!
        baseCfg.putUserData(EXTRA_KEY, "v1")

        // create from scratch (configs = null but valueType = Block => configs should be emptyList, not null)
        run {
            val created = CwtPropertyConfig.create(
                baseCfg.pointer, baseCfg.configGroup,
                baseCfg.key, baseCfg.value, baseCfg.valueType, baseCfg.separatorType,
                null, baseCfg.optionConfigs
            )
            assertEquals(baseCfg.key, created.key)
            assertEquals(baseCfg.value, created.value)
            assertEquals(CwtType.Block, created.valueType)
            assertNotNull(created.configs)
            assertTrue(created.configs!!.isEmpty())
            // userData is not auto-filled on create
            assertNull(created.getUserData(EXTRA_KEY))
        }

        // copy with overrides (should NOT copy arbitrary userData)
        run {
            val copied = CwtPropertyConfig.copy(
                baseCfg,
                key = baseCfg.key + "_c",
                value = baseCfg.value,
                valueType = baseCfg.valueType,
                separatorType = CwtSeparatorType.EQUAL,
                configs = baseCfg.configs,
                optionConfigs = baseCfg.optionConfigs
            )
            assertEquals(baseCfg.key + "_c", copied.key)
            assertEquals(baseCfg.value, copied.value)
            assertEquals(baseCfg.valueType, copied.valueType)
            assertEquals(CwtSeparatorType.EQUAL, copied.separatorType)
            assertEquals(baseCfg.configs?.size, copied.configs?.size)
            assertEquals(baseCfg.optionConfigs?.size, copied.optionConfigs?.size)
            assertNull(copied.getUserData(EXTRA_KEY))
        }

        // delegated (inherit read of userData; parentConfig should be reset; configs can be replaced)
        run {
            val delegated = CwtPropertyConfig.delegated(baseCfg, configs = emptyList())
            assertNull(delegated.parentConfig)
            assertNotNull(delegated.configs)
            assertTrue(delegated.configs!!.isEmpty())
            // inherit read from target (wrapper defers to delegate if present)
            assertEquals("v1", delegated.getUserData(EXTRA_KEY))
            // write wrapper-only data using another key
            val EXTRA_KEY_2: Key<String> = createKey("test.extra.property.2")
            delegated.putUserData(EXTRA_KEY_2, "v2")
            assertEquals("v2", delegated.getUserData(EXTRA_KEY_2))
            assertNull(baseCfg.getUserData(EXTRA_KEY_2))
        }

        // delegatedWith (override key/value; expressions recomputed)
        run {
            val delegated = CwtPropertyConfig.delegatedWith(baseCfg, key = baseCfg.key + "_d", value = "42")
            assertEquals(baseCfg.key + "_d", delegated.key)
            assertEquals("42", delegated.value)
            // expressions exist; for block value (configs != null), valueExpression is blockExpression with isKey=true
            assertTrue(delegated.keyExpression.isKey)
            assertTrue(delegated.valueExpression.isKey)
        }
    }

    // endregion

    @Test
    fun testResolver_delegatedWith_nonBlock_expressions_and_parent() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!
        val p = root.findChild<CwtProperty> { it.name == "str_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!
        assertEquals(CwtType.String, c.valueType)
        val d = CwtPropertyConfig.delegatedWith(c, key = c.key + "_d", value = "x")
        assertNull(d.parentConfig)
        assertTrue(d.keyExpression.isKey)
        assertFalse(d.valueExpression.isKey)
    }
}
