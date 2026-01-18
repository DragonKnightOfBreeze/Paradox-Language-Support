package icu.windea.pls.config.util

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.core.findChild
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtMemberConfigVisitorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private fun preparePropertyCases(): Pair<CwtFile, CwtConfigGroup> {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        return file to group
    }

    private fun prepareValueCases(): Pair<CwtFile, CwtConfigGroup> {
        myFixture.configureByFile("features/config/value_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        return file to group
    }

    @Test
    fun testDispatch_visitProperty_and_visitValue() {
        val (file1, group1) = preparePropertyCases()
        val p = file1.block!!.findChild<CwtProperty> { it.name == "str_prop" }!!
        val pConfig = CwtPropertyConfig.resolve(p, file1, group1)!!

        val calls1 = mutableListOf<String>()
        val visitor1 = object : CwtMemberConfigVisitor() {
            override fun visit(config: icu.windea.pls.config.config.CwtMemberConfig<*>): Boolean {
                calls1 += "member"
                return true
            }

            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                calls1 += "property"
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                calls1 += "value"
                return super.visitValue(config)
            }
        }
        assertTrue(pConfig.accept(visitor1))
        assertEquals(listOf("property", "member"), calls1)

        val (file2, group2) = prepareValueCases()
        val v = file2.block!!.findChild<CwtValue> { it.value == "yes" }!!
        val vConfig = CwtValueConfig.resolve(v, file2, group2)

        val calls2 = mutableListOf<String>()
        val visitor2 = object : CwtMemberConfigVisitor() {
            override fun visit(config: icu.windea.pls.config.config.CwtMemberConfig<*>): Boolean {
                calls2 += "member"
                return true
            }

            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                calls2 += "property"
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                calls2 += "value"
                return super.visitValue(config)
            }
        }
        assertTrue(vConfig.accept(visitor2))
        assertEquals(listOf("value", "member"), calls2)
    }

    @Test
    fun testAcceptChildren_traversesDirectChildrenOnly() {
        val (file, group) = preparePropertyCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "block_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        val visitor = object : CwtMemberConfigVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                return true
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visited += "V:${config.value}"
                return true
            }
        }

        assertTrue(c.acceptChildren(visitor))
        assertEquals(listOf("P:a", "V:val1"), visited)

        val p2 = file.block!!.findChild<CwtProperty> { it.name == "str_prop" }!!
        val c2 = CwtPropertyConfig.resolve(p2, file, group)!!
        val visited2 = mutableListOf<String>()
        val visitor2 = object : CwtMemberConfigVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited2 += "P:${config.key}"
                return true
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visited2 += "V:${config.value}"
                return true
            }
        }
        assertTrue(c2.acceptChildren(visitor2))
        assertTrue(visited2.isEmpty())
    }

    @Test
    fun testAcceptChildren_shortCircuitsOnFalse() {
        val (file, group) = preparePropertyCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "block_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        val visitor = object : CwtMemberConfigVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                return false
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visited += "V:${config.value}"
                return true
            }
        }

        assertFalse(c.acceptChildren(visitor))
        assertEquals(listOf("P:a"), visited)
    }
}
