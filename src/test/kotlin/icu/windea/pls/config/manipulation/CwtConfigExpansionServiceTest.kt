package icu.windea.pls.config.manipulation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.expandBySubtypeExpression
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
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

    // region union

    @Test
    fun testExpandUnion_name() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        val result = CwtConfigExpansionService.expandUnion("test_union", configGroup, guardName = "test.expandUnion")
        val expressions = result.map { it.configExpression.expressionString }.toList()
        assertEquals(listOf("int", "value[test_flag]"), expressions)
        assertTrue(result.all { it.config.configExpression == it.configExpression })
    }

    @Test
    fun testExpandUnion_configExpression() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val configExpression = CwtDataExpression.resolve("union[test_union]", CwtDataExpressionRole.Value)

        val result = CwtConfigExpansionService.expandUnion(configExpression, configGroup, guardName = "test.expandUnion")
        val expressions = result.map { it.configExpression.expressionString }.toList()
        assertEquals(listOf("int", "value[test_flag]"), expressions)
    }

    @Test
    fun testExpandUnion_preservesDeclarationOrderAndDuplicates() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        val mixed = CwtConfigExpansionService.expandUnion("test_union_mixed", configGroup, guardName = "test.expandUnion")
        assertEquals(listOf("v1", "int"), mixed.map { it.configExpression.expressionString }.toList())

        val duplicated = CwtConfigExpansionService.expandUnion("test_union_duplicate", configGroup, guardName = "test.expandUnion")
        assertEquals(listOf("v1", "v1"), duplicated.map { it.configExpression.expressionString }.toList())
        assertEquals(2, duplicated.toList().size)
    }

    // endregion

    // region alias

    @Test
    fun testExpandAlias_name() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        val result = CwtConfigExpansionService.expandAlias("test_alias", configGroup, guardName = "test.expandAlias").toList()
        assertEquals(listOf("int", "scalar", "foo", "bar"), result.map { it.configExpression.expressionString })
        val foo = result.first { it.configExpression.expressionString == "foo" }
        assertEquals(2, foo.configs.size)
        assertSame(foo.config, foo.configs.first())
    }

    @Test
    fun testExpandAlias_configExpression() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val keysFieldExpression = CwtDataExpression.resolve("alias_keys_field[test_alias]", CwtDataExpressionRole.Key)
        val nameExpression = CwtDataExpression.resolve("alias_name[test_alias]", CwtDataExpressionRole.Key)
        val expected = listOf("int", "scalar", "foo", "bar")

        val byKeysField = CwtConfigExpansionService.expandAlias(keysFieldExpression, configGroup, guardName = "test.expandAlias")
        val byName = CwtConfigExpansionService.expandAlias(nameExpression, configGroup, guardName = "test.expandAlias")
        assertEquals(expected, byKeysField.map { it.configExpression.expressionString }.toList())
        assertEquals(expected, byName.map { it.configExpression.expressionString }.toList())
    }

    // endregion

    // region singleAlias

    @Test
    fun testExpandSingleAlias_name() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        val result = CwtConfigExpansionService.expandSingleAlias("test_single_alias", configGroup, guardName = "test.expandSingleAlias").toList()
        assertEquals(1, result.size)
        val expanded = result.single()
        assertEquals("int", expanded.configExpression.expressionString)
        assertEquals("test_single_alias", expanded.config.name)
    }

    @Test
    fun testExpandSingleAlias_configExpression() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val configExpression = CwtDataExpression.resolve("single_alias_right[test_single_alias]", CwtDataExpressionRole.Value)

        val result = CwtConfigExpansionService.expandSingleAlias(configExpression, configGroup, guardName = "test.expandSingleAlias").toList()
        assertEquals(1, result.size)
        assertEquals("int", result.single().configExpression.expressionString)
    }

    // endregion

    // region expandable

    @Test
    fun testExpandExpandable() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        val unionExpression = CwtDataExpression.resolve("union[test_union_constant]", CwtDataExpressionRole.Value)
        val unionResult = CwtConfigExpansionService.expandExpandable(unionExpression, configGroup, guardName = "test.expandExpandable")
        assertEquals(listOf("v1", "v2"), unionResult.map { it.configExpression.expressionString }.toList())

        val aliasExpression = CwtDataExpression.resolve("alias_keys_field[test_alias]", CwtDataExpressionRole.Key)
        val aliasResult = CwtConfigExpansionService.expandExpandable(aliasExpression, configGroup, guardName = "test.expandExpandable")
        assertEquals(listOf("int", "scalar", "foo", "bar"), aliasResult.map { it.configExpression.expressionString }.toList())

        val singleAliasExpression = CwtDataExpression.resolve("single_alias_right[test_single_alias]", CwtDataExpressionRole.Value)
        val singleAliasResult = CwtConfigExpansionService.expandExpandable(singleAliasExpression, configGroup, guardName = "test.expandExpandable")
        assertEquals(listOf("int"), singleAliasResult.map { it.configExpression.expressionString }.toList())
    }

    @Test
    fun testExpandExpandable_nonExpandable_returnsEmpty() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val configExpression = CwtDataExpression.resolve("int", CwtDataExpressionRole.Key)

        val result = CwtConfigExpansionService.expandExpandable(configExpression, configGroup, guardName = "test.expandExpandable")
        assertTrue(result.toList().isEmpty())
    }

    // endregion
}
