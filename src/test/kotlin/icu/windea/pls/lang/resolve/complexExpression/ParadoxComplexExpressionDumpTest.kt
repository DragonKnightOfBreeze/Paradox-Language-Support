package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.base.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxComplexExpressionDumpTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Vic3)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun parseScope(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxScopeFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        PlsStates.incompleteComplexExpression.remove()
        return ParadoxScopeFieldExpression.resolve(text, null, configGroup)
    }

    private fun parseValue(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxValueFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxValueFieldExpression.resolve(text, null, configGroup)
    }

    private fun parseVariable(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxVariableFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        PlsStates.incompleteComplexExpression.remove()
        return ParadoxVariableFieldExpression.resolve(text, null, configGroup)
    }

    private fun parseDb(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxDatabaseObjectExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        PlsStates.incompleteComplexExpression.remove()
        return ParadoxDatabaseObjectExpression.resolve(text, null, configGroup)
    }

    private fun parseDefine(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxDefineReferenceExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        PlsStates.incompleteComplexExpression.remove()
        return ParadoxDefineReferenceExpression.resolve(text, null, configGroup)
    }

    private fun parseCommand(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris): ParadoxCommandExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        PlsStates.incompleteComplexExpression.remove()
        return ParadoxCommandExpression.resolve(text, null, configGroup)
    }

    // ScopeField samples
    @Test
    fun test_scope_basicSamples() {
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
    @Test
    fun test_value_basicSamples() {
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
    @Test
    fun test_variable_basicSamples() {
        val s = "root.owner.some_variable"
        val exp = parseVariable(s)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }

    // DatabaseObject samples
    @Test
    fun test_databaseObject_basicSamples() {
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
    @Test
    fun test_defineReference_basicSamples() {
        val s = "define:NPortrait|GRACEFUL_AGING_START"
        val exp = parseDefine(s)!!
        val out = exp.render()
        println(out)
        Assert.assertTrue(out.isNotBlank())
    }

    // Command samples
    @Test
    fun test_command_basicSamples() {
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
    @Test
    fun test_value_incomplete_trimmedCompare() {
        val s = ""
        val exp = parseValue(s, incomplete = true)!!
        val out1 = exp.render()
        val out2 = ParadoxComplexExpressionDslRenderer.render(exp, ParadoxComplexExpressionDslRenderer.Options(trimEmptyNodes = true))
        println("default:\n$out1")
        println("trimmed:\n$out2")
        Assert.assertTrue(out1.length >= out2.length)
    }
}
