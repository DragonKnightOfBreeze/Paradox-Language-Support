package icu.windea.pls.config.config

import com.intellij.openapi.util.Key
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.findChild
import icu.windea.pls.core.util.createKey
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtValueConfigTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private fun prepare(): Triple<CwtFile, CwtConfigGroup, String> {
        myFixture.configureByFile("features/config/value_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroup(project, ParadoxGameType.Stellaris)
        val path = "common/test/value_config_cases.cwt"
        return Triple(file, group, path)
    }

    @Test
    fun testTopLevelValues_and_Options() {
        val prep = prepare()
        val file = prep.first
        val group = prep.second
        val root = file.block!!

        // yes with preceding option
        val yes = root.findChild<CwtValue> { it.value == "yes" }!!
        val yesC = CwtValueConfig.resolve(yes, file, group)
        assertEquals("yes", yesC.value)
        assertEquals(CwtType.Boolean, yesC.valueType)
        assertNotNull(yesC.optionConfigs)
        val hasTag = yesC.optionConfigs.any { it is CwtOptionValueConfig && it.value == "tag" }
        assertTrue(hasTag)

        // 42 int
        val i = root.findChild<CwtValue> { it.value == "42" }!!
        val iC = CwtValueConfig.resolve(i, file, group)
        assertEquals(CwtType.Int, iC.valueType)

        // 3.14 float
        val f = root.findChild<CwtValue> { it.value == "3.14" }!!
        val fC = CwtValueConfig.resolve(f, file, group)
        assertEquals(CwtType.Float, fC.valueType)

        // quoted string -> unquoted value
        val s = root.findChild<CwtValue> { it.value == "sv" }!!
        val sC = CwtValueConfig.resolve(s, file, group)
        assertEquals(CwtType.String, sC.valueType)

        // identifier string
        val ident = root.findChild<CwtValue> { it.value == "ident" }!!
        val identC = CwtValueConfig.resolve(ident, file, group)
        assertEquals(CwtType.String, identC.valueType)
    }

    @Test
    fun testBlockValue_nestedMembers() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!
        val block = root.findChild<CwtValue> { it.value == "{...}" }!!
        val c = CwtValueConfig.resolve(block, file, group)
        assertEquals(CwtType.Block, c.valueType)
        assertNotNull(c.configs)
        // a(property) and val1(value)
        assertEquals(2, c.configs!!.size)
    }

    @Test
    fun testBoundaries_values_and_Options() {
        myFixture.configureByFile("features/config/value_config_boundaries.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroup(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        // option value with space, preceding a plain identifier value
        run {
            val ident = root.findChild<CwtValue> { it.value == "ident_val" }!!
            val c = CwtValueConfig.resolve(ident, file, group)
            assertEquals(CwtType.String, c.valueType)
            val opts = c.optionConfigs
            assertNotNull(opts)
            assertTrue(opts.any { it is CwtOptionValueConfig && it.value == "note with space" })
        }

        // number forms
        run {
            val v = root.findChild<CwtValue> { it.value == "-7" }!!
            val c = CwtValueConfig.resolve(v, file, group)
            assertEquals(CwtType.Int, c.valueType)
        }
        run {
            val v = root.findChild<CwtValue> { it.value == "-.25" }!!
            val c = CwtValueConfig.resolve(v, file, group)
            assertEquals(CwtType.Float, c.valueType)
        }
        run {
            val v = root.findChild<CwtValue> { it.value == ".5" }!!
            val c = CwtValueConfig.resolve(v, file, group)
            assertEquals(CwtType.Float, c.valueType)
        }
        run {
            val v = root.findChild<CwtValue> { it.value == "007" }!!
            val c = CwtValueConfig.resolve(v, file, group)
            assertEquals(CwtType.Int, c.valueType)
        }
        run {
            val v = root.findChild<CwtValue> { it.value == " spaced " }!!
            val c = CwtValueConfig.resolve(v, file, group)
            assertEquals(CwtType.String, c.valueType)
        }

        // empty block value
        run {
            val v1 = root.findChild<CwtValue> { it.value == "{...}" }!!
            val c = CwtValueConfig.resolve(v1, file, group)
            assertEquals(CwtType.Block, c.valueType)
            assertNotNull(c.configs)
            assertTrue(c.configs!!.isEmpty())
        }

        // block value with option comment containing nested option members (## meta = { inner = 1 foo })
        run {
            val blocks = PsiTreeUtil.findChildrenOfType(file, CwtValue::class.java).filter { it.value == "{...}" }
            val target = blocks.first { b ->
                val cfg = CwtValueConfig.resolve(b, file, group)
                val opts = cfg.optionConfigs
                opts.filterIsInstance<CwtOptionConfig>().any { it.key == "meta" }
            }
            val c = CwtValueConfig.resolve(target, file, group)
            assertEquals(CwtType.Block, c.valueType)
            val meta = c.optionConfigs.filterIsInstance<CwtOptionConfig>().single { it.key == "meta" }
            assertEquals(CwtType.Block, meta.valueType)
            val nested = meta.optionConfigs
            assertNotNull(nested)
            assertTrue(nested!!.filterIsInstance<CwtOptionConfig>().any { it.key == "inner" && it.value == "1" })
            assertTrue(nested.any { it is CwtOptionValueConfig && it.value == "foo" })
        }
    }

    // region Resolver: create/copy/delegated/delegatedWith/resolveFromPropertyConfig + userData

    private val extraKey: Key<String> = createKey("test.extra.value")

    @Test
    fun testResolver_create_copy_delegated_forValue() {
        val prep = prepare()
        val file = prep.first
        val group = prep.second
        val root = file.block!!

        // base value (block)
        val base = root.findChild<CwtValue> { it.value == "{...}" }!!
        val baseCfg = CwtValueConfig.resolve(base, file, group)
        baseCfg.putUserData(extraKey, "vv1")

        // create from scratch (configs = null but valueType = Block => configs should be emptyList, not null)
        run {
            val created = CwtValueConfig.create(
                baseCfg.pointer, baseCfg.configGroup,
                baseCfg.value, baseCfg.valueType,
                null, baseCfg.optionConfigs, null
            )
            assertEquals(baseCfg.value, created.value)
            assertEquals(CwtType.Block, created.valueType)
            assertNotNull(created.configs)
            assertTrue(created.configs!!.isEmpty())
            // userData is not autofilled on create
            assertNull(created.getUserData(extraKey))
        }

        // copy with overrides (should NOT copy arbitrary userData)
        run {
            val copied = CwtValueConfig.copy(
                baseCfg,
                value = baseCfg.value,
                valueType = baseCfg.valueType,
                configs = baseCfg.configs,
                optionConfigs = baseCfg.optionConfigs,
                propertyConfig = baseCfg.propertyConfig
            )
            assertEquals(baseCfg.value, copied.value)
            assertEquals(baseCfg.valueType, copied.valueType)
            assertEquals(baseCfg.configs?.size, copied.configs?.size)
            assertEquals(baseCfg.optionConfigs.size, copied.optionConfigs.size)
            assertNull(copied.getUserData(extraKey))
        }

        // delegated (inherit read of userData; parentConfig should be reset; configs can be replaced)
        run {
            val delegated = CwtValueConfig.delegated(baseCfg, configs = emptyList())
            assertNull(delegated.parentConfig)
            assertNotNull(delegated.configs)
            assertTrue(delegated.configs!!.isEmpty())
            // inherit read from target (wrapper defers to delegate if present)
            assertEquals("vv1", delegated.getUserData(this.extraKey))
            // write wrapper-only data using another key
            val extraKey2: Key<String> = createKey("test.extra.value.2")
            delegated.putUserData(extraKey2, "vv2")
            assertEquals("vv2", delegated.getUserData(extraKey2))
            assertNull(baseCfg.getUserData(extraKey2))
        }

        // delegatedWith (override value; expressions recomputed)
        run {
            val delegated = CwtValueConfig.delegatedWith(baseCfg, value = "over")
            assertEquals("over", delegated.value)
            // for block base (configs != null), valueExpression is blockExpression with isKey=false
            assertFalse(delegated.valueExpression.isKey)
        }
    }

    @Test
    fun testResolver_resolveFromPropertyConfig_valueWrapper_userDataIsolation() {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroup(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val prop = root.findChild<CwtProperty> { it.name == "block_prop" }!!
        val pCfg = CwtPropertyConfig.resolve(prop, file, group)!!
        pCfg.putUserData(extraKey, "pv1")

        val vPtr = prop.propertyValue!!.createPointer(file)
        val vCfg = CwtValueConfig.resolveFromPropertyConfig(vPtr, pCfg)

        assertSame(pCfg, vCfg.propertyConfig)
        assertEquals(pCfg.value, vCfg.value)
        assertEquals(pCfg.valueType, vCfg.valueType)
        assertEquals(pCfg.configs?.size, vCfg.configs?.size)
        assertEquals(pCfg.optionConfigs.size, vCfg.optionConfigs.size)
        // for block property, wrapper valueExpression is blockExpression with isKey=false
        assertFalse(vCfg.valueExpression.isKey)
        // userData should NOT be inherited from property config on wrapper
        assertNull(vCfg.getUserData(extraKey))
        vCfg.putUserData(extraKey, "vw1")
        assertEquals("vw1", vCfg.getUserData(extraKey))
        assertEquals("pv1", pCfg.getUserData(extraKey))
    }

    @Test
    fun testResolver_delegatedWith_nonBlock_expressions_forValue() {
        val (file, group) = prepare().let { it.first to it.second }
        val root = file.block!!
        // pick non-block value: 42
        val v = root.findChild<CwtValue> { it.value == "42" }!!
        val c = CwtValueConfig.resolve(v, file, group)
        assertEquals(CwtType.Int, c.valueType)
        val d = CwtValueConfig.delegatedWith(c, value = "100")
        assertNull(d.parentConfig)
        assertEquals("100", d.value)
        assertFalse(d.valueExpression.isKey)
    }

    // endregion
}
