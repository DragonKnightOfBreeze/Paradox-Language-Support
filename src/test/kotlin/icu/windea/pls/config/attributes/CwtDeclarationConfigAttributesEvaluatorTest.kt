package icu.windea.pls.config.attributes

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtDeclarationConfigAttributesEvaluatorTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/config/attributes")
        markConfigDirectory("features/config/attributes/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun loadDeclarations(): Map<String, CwtDeclarationConfig> {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return configGroup.declarations
    }

    @Test
    fun testSimpleDeclaration_noInvolvedTypes() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_simple"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 应该没有任何涉及类型
        Assert.assertTrue(attributes.involvedSubtypes.isEmpty())
        Assert.assertFalse(attributes.dynamicValueInvolved)
        Assert.assertFalse(attributes.parameterInvolved)
        Assert.assertFalse(attributes.localisationParameterInvolved)
        Assert.assertEquals(CwtDeclarationConfigAttributes.EMPTY, attributes)
    }

    @Test
    fun testDeclarationWithSubtypes_onlySubtypes() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_with_subtypes"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 应该包含两个子类型
        Assert.assertEquals(setOf("variant_a", "variant_b"), attributes.involvedSubtypes)
        Assert.assertFalse(attributes.dynamicValueInvolved)
        Assert.assertFalse(attributes.parameterInvolved)
        Assert.assertFalse(attributes.localisationParameterInvolved)
    }

    @Test
    fun testDeclarationWithDynamicValue_variousVariants() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_with_dynamic_value"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 应该检测到动态值涉及（value[x], variable_field, scope_field, value_set[]）
        Assert.assertTrue(attributes.dynamicValueInvolved)
        Assert.assertFalse(attributes.parameterInvolved)
        Assert.assertFalse(attributes.localisationParameterInvolved)
    }

    @Test
    fun testDeclarationWithParameter_detected() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_with_parameter"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 应该检测到参数涉及
        Assert.assertFalse(attributes.dynamicValueInvolved)
        Assert.assertTrue(attributes.parameterInvolved)
        Assert.assertFalse(attributes.localisationParameterInvolved)
    }

    @Test
    fun testDeclarationWithLocalisationParameter_detected() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_with_loc_parameter"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 应该检测到本地化参数涉及
        Assert.assertFalse(attributes.dynamicValueInvolved)
        Assert.assertFalse(attributes.parameterInvolved)
        Assert.assertTrue(attributes.localisationParameterInvolved)
    }

    @Test
    fun testDeclarationWithSingleAlias_inlinedTypesDetected() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_with_single_alias"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // single_alias 内联后，应该检测到其中的动态值（alias 中有 value[test_check]）
        Assert.assertTrue(attributes.dynamicValueInvolved)
    }

    @Test
    fun testDeclarationWithAlias_inlinedTypesDetected() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_with_alias"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // alias_keys_field 内联后，应该检测到其中的动态值和scope（alias 中有 value[x] 和 scope_field）
        Assert.assertTrue(attributes.dynamicValueInvolved)
    }

    @Test
    fun testComplexDeclaration_allTypesDetected() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_complex"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 应该检测到所有类型
        Assert.assertEquals(setOf("type_a", "type_b"), attributes.involvedSubtypes)
        Assert.assertTrue(attributes.dynamicValueInvolved)
        Assert.assertTrue(attributes.parameterInvolved)
        Assert.assertTrue(attributes.localisationParameterInvolved)
    }

    @Test
    fun testInlinedConfigsWithInvolvedTypes_detectedRecursively() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_inlined_involved"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 虽然直接属性没有动态值，但内联的 single_alias 包含动态值和参数
        Assert.assertTrue(attributes.dynamicValueInvolved)
        Assert.assertTrue(attributes.parameterInvolved)
    }

    @Test
    fun testDeepNestedInlining_detectedRecursively() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_deep_nested"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 深度嵌套：sa_level1 -> sa_level2 -> value[deep_value]
        Assert.assertTrue(attributes.dynamicValueInvolved)
    }

    @Test
    fun testSubtypesNotInInlinedConfigs_onlyDirectSubtypes() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_complex"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // involvedSubtypes 应该只包含直接的 subtype[...]，不包含内联配置中的
        Assert.assertEquals(setOf("type_a", "type_b"), attributes.involvedSubtypes)
        // 确保没有来自内联配置的假子类型
        Assert.assertFalse(attributes.involvedSubtypes.contains("simple"))
        Assert.assertFalse(attributes.involvedSubtypes.contains("with_parameter"))
    }

    @Test
    fun testEmptyAttributesOptimization_returnsSharedInstance() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_simple"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 当所有属性都为空时，应该返回共享的 EMPTY 实例
        Assert.assertSame(CwtDeclarationConfigAttributes.EMPTY, attributes)
    }

    @Test
    fun testMultipleEvaluations_consistent() {
        val declarations = loadDeclarations()
        val decl = declarations["test_decl_complex"]
        Assert.assertNotNull(decl)
        decl!!

        val attributes1 = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)
        val attributes2 = CwtDeclarationConfigAttributesEvaluator().evaluate(decl)

        // 多次评估应该得到一致的结果
        Assert.assertEquals(attributes1.involvedSubtypes, attributes2.involvedSubtypes)
        Assert.assertEquals(attributes1.dynamicValueInvolved, attributes2.dynamicValueInvolved)
        Assert.assertEquals(attributes1.parameterInvolved, attributes2.parameterInvolved)
        Assert.assertEquals(attributes1.localisationParameterInvolved, attributes2.localisationParameterInvolved)
    }
}
