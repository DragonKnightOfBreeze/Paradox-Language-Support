package icu.windea.pls.config.util

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtMemberConfig
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
class CwtMemberConfigRecursiveVisitorTest : BasePlatformTestCase() {
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

    private fun label(config: CwtMemberConfig<*>): String {
        return when (config) {
            is CwtPropertyConfig -> "P:${config.key}"
            is CwtValueConfig -> "V:${config.value}"
            else -> "M"
        }
    }

    @Test
    fun testTraversalOrder_onPropertyBlock() {
        val (file, group) = preparePropertyCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "block_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!

        val started = mutableListOf<String>()
        val finished = mutableListOf<String>()

        val visitor = object : CwtMemberConfigRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                started += "P:${config.key}"
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                started += "V:${config.value}"
                return super.visitValue(config)
            }

            override fun visitFinished(config: CwtMemberConfig<*>): Boolean {
                finished += label(config)
                return true
            }
        }

        assertTrue(c.accept(visitor))
        assertEquals(listOf("P:block_prop", "P:a", "V:val1"), started)
        assertEquals(listOf("P:a", "V:val1", "P:block_prop"), finished)
    }

    @Test
    fun testTraversalOrder_onValueBlock() {
        val (file, group) = prepareValueCases()
        val v = file.block!!.findChild<CwtValue> { it.value == "{...}" }!!
        val c = CwtValueConfig.resolve(v, file, group)

        val started = mutableListOf<String>()
        val finished = mutableListOf<String>()

        val visitor = object : CwtMemberConfigRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                started += "P:${config.key}"
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                started += "V:${config.value}"
                return super.visitValue(config)
            }

            override fun visitFinished(config: CwtMemberConfig<*>): Boolean {
                finished += label(config)
                return true
            }
        }

        assertTrue(c.accept(visitor))
        assertEquals(listOf("V:{...}", "P:a", "V:val1"), started)
        assertEquals(listOf("P:a", "V:val1", "V:{...}"), finished)
    }

    @Test
    fun testStopTraversal_preventsSiblingsAndParentFinished() {
        val (file, group) = preparePropertyCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "block_prop" }!!
        val c = CwtPropertyConfig.resolve(p, file, group)!!

        val started = mutableListOf<String>()
        val finished = mutableListOf<String>()

        val visitor = object : CwtMemberConfigRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                started += "P:${config.key}"
                if (config.key == "a") return false
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                started += "V:${config.value}"
                return super.visitValue(config)
            }

            override fun visitFinished(config: CwtMemberConfig<*>): Boolean {
                finished += label(config)
                return true
            }
        }

        assertFalse(c.accept(visitor))
        assertEquals(listOf("P:block_prop", "P:a"), started)
        assertTrue(finished.isEmpty())
    }
}
