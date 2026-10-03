package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxTemplateExpression
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxTemplateExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, template: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxTemplateExpression? {
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val config = CwtValueConfig.mock(configGroup, template)
        return markIncomplete(incomplete) { ParadoxTemplateExpression.resolve(text, null, configGroup, config) }
    }

    private fun pickModifierWithTemplate(gameType: ParadoxGameType, predicate: (CwtModifierConfig) -> Boolean): CwtModifierConfig? {
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        return configGroup.modifiers.values.toList().firstOrNull(predicate)
    }

    @Test
    fun template_job_placeholder_test() {
        val gameType = ParadoxGameType.Stellaris
        val config = pickModifierWithTemplate(gameType) { it.template.expressionString.contains("<") }
        Assume.assumeTrue("No modifier with <placeholder> template found", config != null)
        val tpl = config!!.template
        // 构造一个简单匹配文本：将所有占位替换为 foo
        val text = if (tpl.referenceExpressions.size == 1) {
            CwtConfigExpressionManager.extract(tpl, "foo")
        } else {
            val refMap = tpl.referenceExpressions.associateWith { "foo" }
            CwtConfigExpressionManager.extract(tpl, refMap)
        }
        val g = ChronicleFacade.getConfigGroup(project, gameType)
        val exp = ParadoxTemplateExpression.resolve(text, null, g, config)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.contains("ParadoxTemplateSnippetConstantNode") && out.contains("ParadoxTemplateSnippetNode"))
    }

    @Test
    fun template_enum_placeholder_dumpOnly_test() {
        val gameType = ParadoxGameType.Stellaris
        val config = pickModifierWithTemplate(gameType) { it.template.expressionString.contains("enum[") }
        Assume.assumeTrue("No modifier with enum[...] in template found", config != null)
        val tpl = config!!.template
        // 仅在单占位时生成用例，否则跳过
        Assume.assumeTrue(tpl.referenceExpressions.size == 1)
        val text = CwtConfigExpressionManager.extract(tpl, "foo")
        val g = ChronicleFacade.getConfigGroup(project, gameType)
        val exp = ParadoxTemplateExpression.resolve(text, null, g, config)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }

    @Test
    fun basic_test_1() {
        val s = "job_solder_add"
        val template = "job_<job>_add"
        val exp = resolve(s, template, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("job_solder_add", 0, 14) {
            node<ParadoxTemplateSnippetConstantNode>("job_", 0, 4)
            node<ParadoxTemplateSnippetNode>("solder", 4, 10)
            node<ParadoxTemplateSnippetConstantNode>("_add", 10, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun basic_test_2() {
        val s = "add_flag_checked"
        val template = "add_flag_<flag>"
        val exp = resolve(s, template, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("add_flag_checked", 0, 16) {
            node<ParadoxTemplateSnippetConstantNode>("add_flag_", 0, 9)
            node<ParadoxTemplateSnippetNode>("checked", 9, 16)
        }
        exp.check(dsl)
    }

    // region complete mode

    @Test
    fun complete_test_ignoreCase() {
        val s = "JOB_SOLDER_ADD"
        val template = "job_<job>_add"
        val exp = resolve(s, template, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("JOB_SOLDER_ADD", 0, 14) {
            node<ParadoxTemplateSnippetConstantNode>("JOB_", 0, 4)
            node<ParadoxTemplateSnippetNode>("SOLDER", 4, 10)
            node<ParadoxTemplateSnippetConstantNode>("_ADD", 10, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun complete_test_leadingDynamicSnippet() {
        val s = "X_b"
        val template = "value[foo]_b"
        val exp = resolve(s, template, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("X_b", 0, 3) {
            node<ParadoxTemplateSnippetNode>("X", 0, 1)
            node<ParadoxTemplateSnippetConstantNode>("_b", 1, 3)
        }
        exp.check(dsl)
    }

    // endregion

    // region incomplete mode - empty dynamic snippets
    // NOTE 完整模式下空的动态片段会被视为无效，因此下列用例使用不完整模式

    @Test
    fun incomplete_test_emptyDynamicSnippet() {
        val s = "a__b"
        val template = "a_value[foo]_b"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("a__b", 0, 4) {
            node<ParadoxTemplateSnippetConstantNode>("a_", 0, 2)
            node<ParadoxTemplateSnippetNode>("", 2, 2)
            node<ParadoxTemplateSnippetConstantNode>("_b", 2, 4)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_leadingDynamicSnippet_empty() {
        val s = "_b"
        val template = "value[foo]_b"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("_b", 0, 2) {
            node<ParadoxTemplateSnippetNode>("", 0, 0)
            node<ParadoxTemplateSnippetConstantNode>("_b", 0, 2)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_adjacentDynamicSnippets() {
        val s = "a_XY_b"
        val template = "a_value[foo]value[bar]_b"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("a_XY_b", 0, 6) {
            node<ParadoxTemplateSnippetConstantNode>("a_", 0, 2)
            node<ParadoxTemplateSnippetNode>("XY", 2, 4)
            node<ParadoxTemplateSnippetNode>("", 4, 4)
            node<ParadoxTemplateSnippetConstantNode>("_b", 4, 6)
        }
        exp.check(dsl)
    }

    // endregion

    // region incomplete mode - constant prefix

    @Test
    fun incomplete_test_constantPrefix_empty() {
        val s = ""
        val template = "any_country_in_<geographic_region_short_key>"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("", 0, 0) {
            node<ParadoxTemplateSnippetConstantNode>("", 0, 0)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_constantPrefix_partial() {
        val s = "any_"
        val template = "any_country_in_<geographic_region_short_key>"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("any_", 0, 4) {
            node<ParadoxTemplateSnippetConstantNode>("any_", 0, 4)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_constantPrefix_full_thenEmptyDynamic() {
        val s = "any_country_in_"
        val template = "any_country_in_<geographic_region_short_key>"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("any_country_in_", 0, 15) {
            node<ParadoxTemplateSnippetConstantNode>("any_country_in_", 0, 15)
            node<ParadoxTemplateSnippetNode>("", 15, 15)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_dynamicPartial() {
        val s = "any_country_in_city_"
        val template = "any_country_in_<geographic_region_short_key>"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("any_country_in_city_", 0, 20) {
            node<ParadoxTemplateSnippetConstantNode>("any_country_in_", 0, 15)
            node<ParadoxTemplateSnippetNode>("city_", 15, 20)
        }
        exp.check(dsl)
    }

    // endregion

    // region incomplete mode - trailing snippets

    @Test
    fun incomplete_test_trailingConstant_stopsAtDynamic() {
        val s = "job_"
        val template = "job_<job>_add"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("job_", 0, 4) {
            node<ParadoxTemplateSnippetConstantNode>("job_", 0, 4)
            node<ParadoxTemplateSnippetNode>("", 4, 4)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_trailingConstant_dynamicPartial() {
        val s = "job_X"
        val template = "job_<job>_add"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("job_X", 0, 5) {
            node<ParadoxTemplateSnippetConstantNode>("job_", 0, 4)
            node<ParadoxTemplateSnippetNode>("X", 4, 5)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_trailingConstant_prefixOfFirstConstant() {
        val s = "job"
        val template = "job_<job>_add"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("job", 0, 3) {
            node<ParadoxTemplateSnippetConstantNode>("job", 0, 3)
        }
        exp.check(dsl)
    }

    @Test
    fun incomplete_test_trailingConstant_fullMatch() {
        val s = "job_X_add"
        val template = "job_<job>_add"
        val exp = resolve(s, template, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTemplateExpression>("job_X_add", 0, 9) {
            node<ParadoxTemplateSnippetConstantNode>("job_", 0, 4)
            node<ParadoxTemplateSnippetNode>("X", 4, 5)
            node<ParadoxTemplateSnippetConstantNode>("_add", 5, 9)
        }
        exp.check(dsl)
    }

    // endregion
}
