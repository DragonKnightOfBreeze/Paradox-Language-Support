package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.util.CwtTemplateExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxTemplateExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    private fun pickModifierWithTemplate(gameType: ParadoxGameType, predicate: (CwtModifierConfig) -> Boolean): CwtModifierConfig? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return configGroup.modifiers.values.toList().firstOrNull(predicate)
    }

    @Test
    fun testTemplate_job_placeholder() {
        val gameType = ParadoxGameType.Stellaris
        val cfg = pickModifierWithTemplate(gameType) { it.template.expressionString.contains("<") }
        Assume.assumeTrue("No modifier with <placeholder> template found", cfg != null)
        val tpl = cfg!!.template
        // 构造一个简单匹配文本：将所有占位替换为 foo
        val text = if (tpl.referenceExpressions.size == 1) {
            CwtTemplateExpressionManager.extract(tpl, "foo")
        } else {
            val refMap = tpl.referenceExpressions.associateWith { "foo" }
            CwtTemplateExpressionManager.extract(tpl, refMap)
        }
        val g = PlsFacade.getConfigGroup(project, gameType)
        val exp = ParadoxTemplateExpression.resolve(text, null, g, cfg)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.contains("ParadoxTemplateSnippetConstantNode") && out.contains("ParadoxTemplateSnippetNode"))
    }

    @Test
    fun testTemplate_enum_placeholder_dumpOnly() {
        val gameType = ParadoxGameType.Stellaris
        val cfg = pickModifierWithTemplate(gameType) { it.template.expressionString.contains("enum[") }
        Assume.assumeTrue("No modifier with enum[...] in template found", cfg != null)
        val tpl = cfg!!.template
        // 仅在单占位时生成用例，否则跳过
        Assume.assumeTrue(tpl.referenceExpressions.size == 1)
        val text = CwtTemplateExpressionManager.extract(tpl, "foo")
        val g = PlsFacade.getConfigGroup(project, gameType)
        val exp = ParadoxTemplateExpression.resolve(text, null, g, cfg)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }
}
