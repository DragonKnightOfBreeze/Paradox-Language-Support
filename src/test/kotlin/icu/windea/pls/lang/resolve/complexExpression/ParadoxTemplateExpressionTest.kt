package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configGroup.modifiers
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert
import org.junit.Assume

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxTemplateExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun pickModifierWithTemplate(gt: ParadoxGameType, predicate: (CwtModifierConfig) -> Boolean): CwtModifierConfig? {
        val g = initConfigGroup(gt)
        return g.modifiers.values.toList().firstOrNull(predicate)
    }

    fun testTemplate_job_placeholder() {
        val gt = ParadoxGameType.Stellaris
        val cfg = pickModifierWithTemplate(gt) { it.template.expressionString.contains("<") }
        Assume.assumeTrue("No modifier with <placeholder> template found", cfg != null)
        val tpl = cfg!!.template
        // 构造一个简单匹配文本：将所有占位替换为 foo
        val text = if (tpl.referenceExpressions.size == 1) {
            CwtTemplateExpressionManager.extract(tpl, "foo")
        } else {
            val refMap = tpl.referenceExpressions.associateWith { "foo" }
            CwtTemplateExpressionManager.extract(tpl, refMap)
        }
        val g = initConfigGroup(gt)
        val exp = ParadoxTemplateExpression.resolve(text, TextRange(0, text.length), g, cfg)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.contains("ParadoxTemplateSnippetConstantNode") && out.contains("ParadoxTemplateSnippetNode"))
    }

    fun testTemplate_enum_placeholder_dumpOnly() {
        val gt = ParadoxGameType.Stellaris
        val cfg = pickModifierWithTemplate(gt) { it.template.expressionString.contains("enum[") }
        Assume.assumeTrue("No modifier with enum[...] in template found", cfg != null)
        val tpl = cfg!!.template
        // 仅在单占位时生成用例，否则跳过
        Assume.assumeTrue(tpl.referenceExpressions.size == 1)
        val text = CwtTemplateExpressionManager.extract(tpl, "foo")
        val g = initConfigGroup(gt)
        val exp = ParadoxTemplateExpression.resolve(text, TextRange(0, text.length), g, cfg)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }
}
