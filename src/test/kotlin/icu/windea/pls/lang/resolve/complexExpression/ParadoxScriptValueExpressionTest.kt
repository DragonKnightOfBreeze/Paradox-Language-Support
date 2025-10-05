package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptValueExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxScriptValueExpression? {
        PlsTestUtil.initConfigGroup(this.project, gameType)
        val group = Unit
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        val linkConfig = group.links["script_value"] ?: error("script_value link not found in config group")
        return ParadoxScriptValueExpression.resolve(text, TextRange(0, text.length), group, linkConfig)
    }

    fun testBasic() {
        val s = "some_sv"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScriptValueExpression>(s, 0..s.length) {
            node<ParadoxScriptValueNode>("some_sv", 0..7)
        }
        exp.check(dsl)
    }

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
