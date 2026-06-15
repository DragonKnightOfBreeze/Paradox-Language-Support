package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptValueExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxScriptValueExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        val linkConfig = configGroup.links["script_value"] ?: error("script_value link not found in config group")
        return ParadoxScriptValueExpression.resolve(text, null, configGroup, linkConfig)
    }

    @Test
    fun test_basic() {
        val s = "some_sv"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScriptValueExpression>(s, 0, s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_withArgs() {
        val s = "some_sv|PARAM|VALUE|"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScriptValueExpression>(s, 0, s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
            node<ParadoxMarkerNode>("|", 7, 8)
            node<ParadoxScriptValueArgumentNode>("PARAM", 8, 13)
            node<ParadoxMarkerNode>("|", 13, 14)
            node<ParadoxScriptValueArgumentValueNode>("VALUE", 14, 19)
            node<ParadoxMarkerNode>("|", 19, 20)
        }
        exp.check(dsl)
    }

    @Test
    fun test_malformed_singlePipe_incompleteAccepted() {
        val s = "some_sv|"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScriptValueExpression>("some_sv|", 0, s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
            node<ParadoxMarkerNode>("|", 7, 8)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty_incompleteDiff() {
        Assert.assertNull(resolve("", incomplete = false))
        val exp = resolve("", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScriptValueExpression>("", 0, 0) {
            node<ParadoxScriptValueNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}
