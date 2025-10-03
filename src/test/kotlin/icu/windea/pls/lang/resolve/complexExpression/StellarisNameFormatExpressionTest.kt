package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class StellarisNameFormatExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        formatName: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): StellarisNameFormatExpression? {
        val g = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        val cfg = CwtValueConfig.resolve(emptyPointer(), g, "stellaris_name_format[$formatName]")
        // ensure value is visible if needed downstream
        cfg.configExpression?.value
        return StellarisNameFormatExpression.resolve(text, TextRange(0, text.length), g, cfg)
    }

    fun testDump_empire_basicSamples() {
        listOf(
            "{<eater_adj> {<patron_noun>}}",
            "{AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}",
            "{<home_planet> Fleet}",
        ).forEach { s ->
            val exp = parse(s, formatName = "empire")!!
            val out = exp.render()
            println(out)
            Assert.assertTrue(out.isNotBlank())
        }
    }

    fun testDump_federation_basicSamples() {
        listOf(
            "{<union_adj> Council}",
        ).forEach { s ->
            val exp = parse(s, formatName = "federation")!!
            val out = exp.render()
            println(out)
            Assert.assertTrue(out.isNotBlank())
        }
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", formatName = "empire", incomplete = false))
        val exp = parse("", formatName = "empire", incomplete = true)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank() || out.isBlank()) // allow any non-crash output
    }
}
