package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
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
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxScriptValueExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        val linkConfig = configGroup.links["script_value"] ?: error("script_value link not found in config group")
        return ParadoxScriptValueExpression.resolve(text, null, configGroup, linkConfig)
    }

    @Test
    fun testBasic() {
        val s = "some_sv"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScriptValueExpression>(s, 0..s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0..7)
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_withArgs() {
        val s = "some_sv|PARAM|VALUE|"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScriptValueExpression>(s, 0..s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0..7)
            node<ParadoxMarkerNode>("|", 7..8)
            node<ParadoxScriptValueArgumentNode>("PARAM", 8..13)
            node<ParadoxMarkerNode>("|", 13..14)
            node<ParadoxScriptValueArgumentValueNode>("VALUE", 14..19)
            node<ParadoxMarkerNode>("|", 19..20)
        }
        exp.check(dsl)
    }

    @Test
    fun testMalformed_singlePipe_incompleteAccepted() {
        val s = "some_sv|"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScriptValueExpression>("some_sv|", 0..s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0..7)
            node<ParadoxMarkerNode>("|", 7..8)
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScriptValueExpression>("", 0..0) {
            node<ParadoxScriptValueNode>("", 0..0)
        }
        exp.check(dsl)
    }
}
