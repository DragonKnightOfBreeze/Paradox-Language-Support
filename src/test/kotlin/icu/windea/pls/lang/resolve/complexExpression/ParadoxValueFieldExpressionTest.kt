package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldValueNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxValueFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxValueFieldExpression? {
        val group = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxValueFieldExpression.resolve(text, TextRange(0, text.length), group)
    }

    fun testTrigger() {
        val s = "trigger:some_trigger"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0..20) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 0..8)
                node<ParadoxValueFieldValueNode>("some_trigger", 8..20) {
                    node<ParadoxDataSourceNode>("some_trigger", 8..20)
                }
            }
        }
        exp.check(dsl)
    }

    fun testScriptValue_basic() {
        val s = "value:some_sv|PARAM|VALUE|"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0..26) {
                node<ParadoxValueFieldPrefixNode>("value:", 0..6)
                node<ParadoxValueFieldValueNode>("some_sv|PARAM|VALUE|", 6..26) {
                    node<ParadoxScriptValueExpression>("some_sv|PARAM|VALUE|", 6..26) {
                        node<ParadoxScriptValueNode>("some_sv", 6..13)
                        node<ParadoxMarkerNode>("|", 13..14)
                        node<ParadoxScriptValueArgumentNode>("PARAM", 14..19)
                        node<ParadoxMarkerNode>("|", 19..20)
                        node<ParadoxScriptValueArgumentValueNode>("VALUE", 20..25)
                        node<ParadoxMarkerNode>("|", 25..26)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    fun testScriptValue_inChain_withDotBefore_andBarrierAfter() {
        val s = "root.value:some_sv|A|B|.owner"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicValueFieldNode>("value:some_sv|A|B|.owner", 5..29) {
                node<ParadoxValueFieldPrefixNode>("value:", 5..11)
                node<ParadoxValueFieldValueNode>("some_sv|A|B|.owner", 11..29) {
                    node<ParadoxScriptValueExpression>("some_sv|A|B|.owner", 11..29) {
                        node<ParadoxScriptValueNode>("some_sv", 11..18)
                        node<ParadoxMarkerNode>("|", 18..19)
                        node<ParadoxScriptValueArgumentNode>("A", 19..20)
                        node<ParadoxMarkerNode>("|", 20..21)
                        node<ParadoxScriptValueArgumentValueNode>("B", 21..22)
                        node<ParadoxMarkerNode>("|", 22..23)
                        node<ParadoxScriptValueArgumentNode>(".owner", 23..29)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    fun testForArgument() {
        val s = "relations(root)"
        val exp = parse(s, gameType = ParadoxGameType.Vic3)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueFieldNode>("relations(root)", 0..15) {
                node<ParadoxValueFieldPrefixNode>("relations", 0..9)
                node<ParadoxOperatorNode>("(", 9..10)
                node<ParadoxValueFieldValueNode>("root", 10..14) {
                    node<ParadoxScopeFieldExpression>("root", 10..14) {
                        node<ParadoxSystemScopeNode>("root", 10..14)
                    }
                }
                node<ParadoxOperatorNode>(")", 14..15)
            }
        }
        exp.check(dsl)
    }

    fun testVariable_inChain() {
        val s = "root.owner.some_variable"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner", 5..10)
            node<ParadoxOperatorNode>(".", 10..11)
            node<ParadoxDynamicValueFieldNode>("some_variable", 11..24) {
                node<ParadoxValueFieldValueNode>("some_variable", 11..24) {
                    node<ParadoxDynamicValueExpression>("some_variable", 11..24) {
                        node<ParadoxDynamicValueNode>("some_variable", 11..24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("", 0..0) {
            node<ParadoxDynamicValueFieldNode>("", 0..0) {
                node<ParadoxValueFieldValueNode>("", 0..0) {
                    node<ParadoxDynamicValueExpression>("", 0..0) {
                        node<ParadoxDynamicValueNode>("", 0..0)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    fun test_forArguments() {
        val s = "root.test_scope(root, some_planet).test_value(some_flag, some_country)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_planet,).test_value(some_flag, some_country,)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root,).test_value(some_flag,)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root).test_value(some_flag)"
        val exp = parse(s)!!
        println(exp.render())
    }

    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_planet).test_value(, some_country)"
        val exp = parse(s)!!
        println(exp.render())
    }
}
