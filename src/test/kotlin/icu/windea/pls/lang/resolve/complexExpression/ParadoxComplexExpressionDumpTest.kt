package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslRender
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxComplexExpressionDumpTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    override fun setUp() {
        super.setUp()
    }

    private fun parseScope(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxScopeFieldExpression? {
        val configGroup = PlsTestUtil.initConfigGroup(project, gameType)
        PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxScopeFieldExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    private fun parseValue(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxValueFieldExpression? {
        val configGroup = PlsTestUtil.initConfigGroup(project, gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxValueFieldExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    private fun parseVariable(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxVariableFieldExpression? {
        val configGroup = PlsTestUtil.initConfigGroup(project, gameType)
        PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxVariableFieldExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    private fun parseDb(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxDatabaseObjectExpression? {
        val configGroup = PlsTestUtil.initConfigGroup(project, gameType)
        PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxDatabaseObjectExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    private fun parseDefine(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxDefineReferenceExpression? {
        val configGroup = PlsTestUtil.initConfigGroup(project, gameType)
        PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxDefineReferenceExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    private fun parseCommand(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxCommandExpression? {
        val configGroup = PlsTestUtil.initConfigGroup(project, gameType)
        PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxCommandExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    // ScopeField samples
    fun testDump_Scope_basicSamples() {
        listOf(
            "root",
            "root.owner",
            "root.owner@x",
            "root.owner|x"
        ).forEach { s ->
            val exp = parseScope(s)!!
            val out = exp.render()
            println(out)
            Assert.assertTrue(out.isNotBlank())
        }
    }

    // ValueField samples (incl. Vic3-only valueField with arguments)
    fun testDump_Value_basicSamples() {
        listOf(
            ParadoxGameType.Stellaris to "trigger:some_trigger",
            ParadoxGameType.Stellaris to "value:some_sv|PARAM|VALUE|",
            ParadoxGameType.Vic3 to "relations(root)",
            ParadoxGameType.Stellaris to "root.owner.some_variable",
        ).forEach { (gt, s) ->
            val exp = parseValue(s, gameType = gt)!!
            val out = exp.render()
            println(out)
            Assert.assertTrue(out.isNotBlank())
        }
    }

    // VariableField samples
    fun testDump_Variable_basicSamples() {
        val s = "root.owner.some_variable"
        val exp = parseVariable(s)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }

    // DatabaseObject samples
    fun testDump_DatabaseObject_basicSamples() {
        listOf(
            "civic:some_civic",
            "civic:some_civic:some_swapped_civic",
            "job:job_soldier",
        ).forEach { s ->
            val exp = parseDb(s)!!
            val out = exp.render()
            println(out)
            Assert.assertTrue(out.isNotBlank())
        }
    }

    // DefineReference samples
    fun testDump_DefineReference_basicSamples() {
        val s = "define:NPortrait|GRACEFUL_AGING_START"
        val exp = parseDefine(s)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }

    // Command samples
    fun testDump_Command_basicSamples() {
        listOf(
            "Root.GetName",
            "Root.Owner.event_target:some_target.var",
        ).forEach { s ->
            val exp = parseCommand(s)!!
            val out = exp.render()
            println(out)
            Assert.assertTrue(out.isNotBlank())
        }
    }

    // Incomplete vs trimmed compare for ValueField
    fun testDump_Value_incomplete_trimmedCompare() {
        val s = ""
        val exp = parseValue(s, incomplete = true)!!
        val out1 = exp.render()
        val out2 = ParadoxComplexExpressionDslRender.render(exp, ParadoxComplexExpressionDslRender.Options(trimEmptyNodes = true))
        println("default:\n$out1")
        println("trimmed:\n$out2")
        Assert.assertTrue(out1.length >= out2.length)
    }
}
