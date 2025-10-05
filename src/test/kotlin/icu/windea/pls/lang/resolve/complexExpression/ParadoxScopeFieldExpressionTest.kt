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
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
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
class ParadoxScopeFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = PlsTestUtil.initConfigGroups(project, ParadoxGameType.Stellaris)

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): ParadoxScopeFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxScopeFieldExpression.resolve(text, TextRange(0, text.length), configGroup)
    }

    @Test
    fun testSingleScopeNode_root() {
        val s = "root"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxSystemScopeNode>("root", 0..4)
        }
        exp.check(dsl)
    }

    @Test
    fun testDotSegmentation_basic() {
        val s = "root.owner"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner", 5..10)
        }
        exp.check(dsl)
    }

    @Test
    fun testEventTarget() {
        val s = "event_target:some_target"
        val exp = parse(s)!!
        println(exp.render())
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

    @Test
    fun testBarrier_At_NoFurtherSplit() {
        val s = "root.owner@x.y"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner@x.y", 5..14)
        }
        exp.check(dsl)
    }

    @Test
    fun testBarrier_Pipe_NoFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner|x.y", 5..14)
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("", 0..0) {
            node<ParadoxErrorScopeLinkNode>("", 0..0)
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments() {
        val s = "root.test_scope(root, some_building)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("root.test_scope(root, some_building)", 0..34) {
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
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_building,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("root.test_scope(root, some_building,)", 0..35) {
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
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("root.test_scope(root)", 0..21) {
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
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("root.test_scope(root,)", 0..22) {
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
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_building)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxScopeFieldExpression>("root.test_scope(, some_building)", 0..30) {
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
        }
        exp.check(dsl)
    }
}
