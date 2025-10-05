package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.expression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScopeFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): ParadoxScopeFieldExpression? {
        val group = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxScopeFieldExpression.resolve(text, TextRange(0, text.length), group)
    }

    fun testSingleScopeNode_root() {
        val s = "root"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxSystemScopeNode>("root", 0..4)
        }
        exp.check(dsl)
    }

    fun testDotSegmentation_basic() {
        val s = "root.owner"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner", 5..10)
        }
        exp.check(dsl)
    }

    fun testEventTarget() {
        val s = "event_target:some_target"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxDynamicScopeLinkNode>("event_target:some_target", 0..24) {
                node<ParadoxScopeLinkPrefixNode>("event_target:", 0..13)
                node<ParadoxScopeLinkValueNode>("some_target", 13..24) {
                    expression<ParadoxDynamicValueExpression>("some_target", 13..24) {
                        node<ParadoxDynamicValueNode>("some_target", 13..24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    fun testBarrier_At_NoFurtherSplit() {
        val s = "root.owner@x.y"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner@x.y", 5..14)
        }
        exp.check(dsl)
    }

    fun testBarrier_Pipe_NoFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner|x.y", 5..14)
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("", 0..0) {
            node<ParadoxErrorScopeLinkNode>("", 0..0)
        }
        exp.check(dsl)
    }

    fun test_forArguments() {
        val s = "root.test_scope(root, some_planet)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_planet,)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root,)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root, some_planet)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(root, some_planet)"
        val exp = parse(s)!!
        println(exp.render())
    }
}
