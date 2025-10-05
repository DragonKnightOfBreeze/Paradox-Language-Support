package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.expression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldValueNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxValueFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = PlsTestUtil.initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Vic3)

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxValueFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxValueFieldExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    @Test
    fun testTrigger() {
        val s = "trigger:some_trigger"
        val exp = parse(s)!!
        println(exp.render())
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

    @Test
    fun testScriptValue_basic() {
        val s = "value:some_sv|PARAM|VALUE|"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0..26) {
                node<ParadoxValueFieldPrefixNode>("value:", 0..6)
                node<ParadoxValueFieldValueNode>("some_sv|PARAM|VALUE|", 6..26) {
                    expression<ParadoxScriptValueExpression>("some_sv|PARAM|VALUE|", 6..26) {
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

    @Test
    fun testScriptValue_inChain_withDotBefore_andBarrierAfter() {
        val s = "root.value:some_sv|A|B|.owner"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicValueFieldNode>("value:some_sv|A|B|.owner", 5..29) {
                node<ParadoxValueFieldPrefixNode>("value:", 5..11)
                node<ParadoxValueFieldValueNode>("some_sv|A|B|.owner", 11..29) {
                    expression<ParadoxScriptValueExpression>("some_sv|A|B|.owner", 11..29) {
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

    @Test
    fun testForArgument() {
        val s = "relations(root)"
        val exp = parse(s, gameType = ParadoxGameType.Vic3)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueFieldNode>("relations(root)", 0..15) {
                node<ParadoxValueFieldPrefixNode>("relations", 0..9)
                node<ParadoxOperatorNode>("(", 9..10)
                node<ParadoxValueFieldValueNode>("root", 10..14) {
                    expression<ParadoxScopeFieldExpression>("root", 10..14) {
                        node<ParadoxSystemScopeNode>("root", 10..14)
                    }
                }
                node<ParadoxOperatorNode>(")", 14..15)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testVariable_inChain() {
        val s = "root.owner.some_variable"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner", 5..10)
            node<ParadoxOperatorNode>(".", 10..11)
            node<ParadoxDynamicValueFieldNode>("some_variable", 11..24) {
                node<ParadoxValueFieldValueNode>("some_variable", 11..24) {
                    expression<ParadoxDynamicValueExpression>("some_variable", 11..24) {
                        node<ParadoxDynamicValueNode>("some_variable", 11..24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("", 0..0) {
            node<ParadoxDynamicValueFieldNode>("", 0..0) {
                node<ParadoxValueFieldValueNode>("", 0..0) {
                    expression<ParadoxDynamicValueExpression>("", 0..0) {
                        node<ParadoxDynamicValueNode>("", 0..0)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments() {
        val s = "root.test_scope(root, some_building).test_value(some_flag, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("root.test_scope(root, some_building).test_value(some_flag, some_job)", 0..70) {
            node<ParadoxSystemScopeNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root, some_building)", 5..34) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5..15)
                node<ParadoxOperatorNode>("(", 15..16)
                node<ParadoxScopeLinkValueNode>("root, some_building", 16..33) {
                    expression<ParadoxScopeFieldExpression>("root", 16..20) {
                        node<ParadoxSystemScopeNode>("root", 16..20)
                    }
                    node<ParadoxMarkerNode>(",", 20..21)
                    node<ParadoxBlankNode>(" ", 21..22)
                    node<ParadoxDataSourceNode>("some_building", 22..33)
                }
                node<ParadoxOperatorNode>(")", 33..34)
            }
            node<ParadoxOperatorNode>(".", 34..35)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job)", 35..70) {
                node<ParadoxValueFieldPrefixNode>("test_value", 35..45)
                node<ParadoxOperatorNode>("(", 45..46)
                node<ParadoxValueFieldValueNode>("some_flag, some_job", 46..69) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 46..55) {
                        node<ParadoxDynamicValueNode>("some_flag", 46..55)
                    }
                    node<ParadoxMarkerNode>(",", 55..56)
                    node<ParadoxBlankNode>(" ", 56..57)
                    node<ParadoxDataSourceNode>("some_job", 57..69)
                }
                node<ParadoxOperatorNode>(")", 69..70)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_building,).test_value(some_flag, some_job,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("root.test_scope(root, some_building,).test_value(some_flag, some_job,)", 0..72) {
            node<ParadoxSystemScopeNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root, some_building,)", 5..35) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5..15)
                node<ParadoxOperatorNode>("(", 15..16)
                node<ParadoxScopeLinkValueNode>("root, some_building,", 16..34) {
                    expression<ParadoxScopeFieldExpression>("root", 16..20) {
                        node<ParadoxSystemScopeNode>("root", 16..20)
                    }
                    node<ParadoxMarkerNode>(",", 20..21)
                    node<ParadoxBlankNode>(" ", 21..22)
                    node<ParadoxDataSourceNode>("some_building", 22..33)
                    node<ParadoxMarkerNode>(",", 33..34)
                }
                node<ParadoxOperatorNode>(")", 34..35)
            }
            node<ParadoxOperatorNode>(".", 35..36)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job,)", 36..72) {
                node<ParadoxValueFieldPrefixNode>("test_value", 36..46)
                node<ParadoxOperatorNode>("(", 46..47)
                node<ParadoxValueFieldValueNode>("some_flag, some_job,", 47..71) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 47..56) {
                        node<ParadoxDynamicValueNode>("some_flag", 47..56)
                    }
                    node<ParadoxMarkerNode>(",", 56..57)
                    node<ParadoxBlankNode>(" ", 57..58)
                    node<ParadoxDataSourceNode>("some_job", 58..70)
                    node<ParadoxMarkerNode>(",", 70..71)
                }
                node<ParadoxOperatorNode>(")", 71..72)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root).test_value(some_flag)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("root.test_scope(root).test_value(some_flag)", 0..43) {
            node<ParadoxSystemScopeNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root)", 5..21) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5..15)
                node<ParadoxOperatorNode>("(", 15..16)
                node<ParadoxScopeLinkValueNode>("root", 16..20) {
                    expression<ParadoxScopeFieldExpression>("root", 16..20) {
                        node<ParadoxSystemScopeNode>("root", 16..20)
                    }
                }
                node<ParadoxOperatorNode>(")", 20..21)
            }
            node<ParadoxOperatorNode>(".", 21..22)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag)", 22..43) {
                node<ParadoxValueFieldPrefixNode>("test_value", 22..32)
                node<ParadoxOperatorNode>("(", 32..33)
                node<ParadoxValueFieldValueNode>("some_flag", 33..42) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 33..42) {
                        node<ParadoxDynamicValueNode>("some_flag", 33..42)
                    }
                }
                node<ParadoxOperatorNode>(")", 42..43)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root,).test_value(some_flag,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("root.test_scope(root,).test_value(some_flag,)", 0..45) {
            node<ParadoxSystemScopeNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root,)", 5..22) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5..15)
                node<ParadoxOperatorNode>("(", 15..16)
                node<ParadoxScopeLinkValueNode>("root,", 16..21) {
                    expression<ParadoxScopeFieldExpression>("root", 16..20) {
                        node<ParadoxSystemScopeNode>("root", 16..20)
                    }
                    node<ParadoxMarkerNode>(",", 20..21)
                }
                node<ParadoxOperatorNode>(")", 21..22)
            }
            node<ParadoxOperatorNode>(".", 22..23)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag,)", 23..45) {
                node<ParadoxValueFieldPrefixNode>("test_value", 23..33)
                node<ParadoxOperatorNode>("(", 33..34)
                node<ParadoxValueFieldValueNode>("some_flag,", 34..44) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 34..43) {
                        node<ParadoxDynamicValueNode>("some_flag", 34..43)
                    }
                    node<ParadoxMarkerNode>(",", 43..44)
                }
                node<ParadoxOperatorNode>(")", 44..45)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_building).test_value(, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxValueFieldExpression>("root.test_scope(, some_building).test_value(, some_job)", 0..57) {
            node<ParadoxSystemScopeNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(, some_building)", 5..30) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5..15)
                node<ParadoxOperatorNode>("(", 15..16)
                node<ParadoxScopeLinkValueNode>(", some_building", 16..29) {
                    node<ParadoxErrorTokenNode>("", 16..16)
                    node<ParadoxMarkerNode>(",", 16..17)
                    node<ParadoxBlankNode>(" ", 17..18)
                    node<ParadoxDataSourceNode>("some_building", 18..29)
                }
                node<ParadoxOperatorNode>(")", 29..30)
            }
            node<ParadoxOperatorNode>(".", 30..31)
            node<ParadoxDynamicValueFieldNode>("test_value(, some_job)", 31..57) {
                node<ParadoxValueFieldPrefixNode>("test_value", 31..41)
                node<ParadoxOperatorNode>("(", 41..42)
                node<ParadoxValueFieldValueNode>(", some_job", 42..56) {
                    node<ParadoxErrorTokenNode>("", 42..42)
                    node<ParadoxMarkerNode>(",", 42..43)
                    node<ParadoxBlankNode>(" ", 43..44)
                    node<ParadoxDataSourceNode>("some_job", 44..56)
                }
                node<ParadoxOperatorNode>(")", 56..57)
            }
        }
        exp.check(dsl)
    }
}
