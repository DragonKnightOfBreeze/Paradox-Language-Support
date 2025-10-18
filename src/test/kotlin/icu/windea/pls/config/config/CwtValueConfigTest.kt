package icu.windea.pls.config.config

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.findChild
import icu.windea.pls.cwt.psi.CwtFile
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
        val hasTag = yesC.optionConfigs!!.any { it is CwtOptionValueConfig && it.value == "tag" }
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
            assertTrue(opts!!.any { it is CwtOptionValueConfig && it.value == "note with space" })
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
                opts != null && opts.filterIsInstance<CwtOptionConfig>().any { it.key == "meta" }
            }
            val c = CwtValueConfig.resolve(target, file, group)
            assertEquals(CwtType.Block, c.valueType)
            val meta = c.optionConfigs!!.filterIsInstance<CwtOptionConfig>().single { it.key == "meta" }
            assertEquals(CwtType.Block, meta.valueType)
            val nested = meta.optionConfigs
            assertNotNull(nested)
            assertTrue(nested!!.filterIsInstance<CwtOptionConfig>().any { it.key == "inner" && it.value == "1" })
            assertTrue(nested.any { it is CwtOptionValueConfig && it.value == "foo" })
        }
    }
}
