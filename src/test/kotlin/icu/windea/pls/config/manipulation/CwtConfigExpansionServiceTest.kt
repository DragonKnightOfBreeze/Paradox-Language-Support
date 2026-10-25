package icu.windea.pls.config.manipulation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.expandBySubtypeExpression
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.core.children
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see CwtConfigExpansionService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigExpansionServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/config/manipulation")
        markConfigDirectory("features/config/manipulation/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testExpandBySubtypeExpression_Basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        myFixture.configureByFile("features/config/manipulation/expand_by_subtype_expression.test.cwt")
        val file = myFixture.file as CwtFile
        val root = file.block!!

        val containerProp = root.children().oneBy<CwtProperty> { it.name == "k1" }!!
        val containerConfig = CwtPropertyConfig.resolve(containerProp, file, configGroup)!!

        val expanded = containerConfig.expandBySubtypeExpression()
        val result = expanded.toList().map { (e1, e2) -> e1.toString() to e2 }
        val expect = listOf(
            tupleOf("(property) k2 = v", ""),
            tupleOf("(property) k3 = v", "t2"),
            tupleOf("(property) k4 = v", "t2&t4"),
            tupleOf("(property) k5 = v", "t2&t4&t5"),
            tupleOf("(property) k6 = v", "t2&t4&t6"),
        )
        assertEquals(expect, result)
    }

    @Test
    fun testExpandConfigExpression_Basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        myFixture.configureByFile("features/config/manipulation/expand_config_expression.test.cwt")
        val file = myFixture.file as CwtFile
        val root = file.block!!

        val propMap = root.propertyList.associateBy({ it.name }, { CwtPropertyConfig.resolve(it, file, configGroup)!! })
        val props = propMap.values

        assertTrue(props.expandConfigExpression().all { it.type == CwtDataTypes.Constant }) // keys -> all constant

        assertFalse(propMap.getValue("k1").expandConfigExpression(forValue = true).all { it.type != CwtDataTypes.Int })
        assertFalse(propMap.getValue("k2").expandConfigExpression(forValue = true).all { it.type != CwtDataTypes.Int }) // expanded
        assertFalse(propMap.getValue("k3").expandConfigExpression(forValue = true).all { it.type != CwtDataTypes.Int }) // expanded
        assertTrue(propMap.getValue("k4").expandConfigExpression(forValue = true).all { it.type != CwtDataTypes.Int }) // ignored
        assertTrue(propMap.getValue("k5").expandConfigExpression(forValue = true).all { it.type != CwtDataTypes.Int }) // ignored
        assertTrue(propMap.getValue("k0").expandConfigExpression(forValue = true).all { it.type != CwtDataTypes.Int })
    }
}
