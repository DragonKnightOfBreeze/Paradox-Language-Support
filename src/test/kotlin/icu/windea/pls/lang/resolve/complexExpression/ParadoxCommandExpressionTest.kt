package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandSuffixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCommandExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxCommandExpression? {
        val group = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxCommandExpression.resolve(text, TextRange(0, text.length), group)
    }

    fun testBasic() {
        val s = "Root.GetName"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxPredefinedCommandFieldNode>("GetName", 5..12)
        }
        exp.check(dsl)
    }

    fun testBasic_chain_noSuffix() {
        val s = "Root.Owner.event_target:some_target.var"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxCommandScopeNode>("Owner", 5..10)
            node<ParadoxOperatorNode>(".", 10..11)
            node<ParadoxDynamicCommandScopeLinkNode>("event_target:some_target", 11..35) {
                node<ParadoxCommandScopeLinkPrefixNode>("event_target:", 11..24)
                node<ParadoxCommandScopeLinkValueNode>("some_target", 24..35) {
                    node<ParadoxDataSourceNode>("some_target", 24..35)
                }
            }
            node<ParadoxOperatorNode>(".", 35..36)
            node<ParadoxDynamicCommandFieldNode>("var", 36..39) {
                node<ParadoxCommandFieldValueNode>("var", 36..39) {
                    node<ParadoxDataSourceNode>("var", 36..39)
                }
            }
        }
        exp.check(dsl)
    }

    fun test_endsWithDot() {
        val s = "Root."
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandFieldNode>("", 5..5) {
                node<ParadoxCommandFieldValueNode>("", 5..5) {
                    node<ParadoxDataSourceNode>("", 5..5)
                }
            }
        }
        exp.check(dsl)
    }

    fun test_endsWithVar() {
        val s = "Root.Var"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandFieldNode>("Var", 5..8) {
                node<ParadoxCommandFieldValueNode>("Var", 5..8) {
                    node<ParadoxDataSourceNode>("Var", 5..8)
                }
            }
        }
        exp.check(dsl)
    }

    fun testWithSuffix_amp() {
        val s = "Root.GetName&L"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxCommandScopeLinkNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxCommandFieldNode>("GetName", 5..12)
            node<ParadoxMarkerNode>("&", 12..13)
            node<ParadoxCommandSuffixNode>("L", 13..14)
        }
        exp.check(dsl)
    }

    fun testWithSuffix_doubleColon() {
        val s = "Root.GetName::UPPER"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxCommandScopeLinkNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxCommandFieldNode>("GetName", 5..12)
            node<ParadoxMarkerNode>("::", 12..14)
            node<ParadoxCommandSuffixNode>("UPPER", 14..19)
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("", 0..0) {
            node<ParadoxDynamicCommandFieldNode>("", 0..0) {
                node<ParadoxCommandFieldValueNode>("", 0..0) {
                    node<ParadoxDataSourceNode>("", 0..0)
                }
            }
        }
        exp.check(dsl)
    }
}
