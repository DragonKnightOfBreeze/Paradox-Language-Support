package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopePrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxStringLiteralNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
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
import icu.windea.pls.model.ParadoxGameType

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScopeFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Vic3)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): ParadoxScopeFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxScopeFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun testSingleScopeNode_root() {
        val s = "root"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0 to s.length) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
        }
        exp.check(dsl)
    }

    @Test
    fun testDotSegmentation_basic() {
        val s = "root.owner"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxScopeNode>("owner", 5 to 10)
        }
        exp.check(dsl)
    }

    @Test
    fun testEventTarget() {
        val s = "event_target:some_target"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0 to s.length) {
            node<ParadoxDynamicScopeNode>("event_target:some_target", 0 to 24) {
                node<ParadoxScopePrefixNode>("event_target:", 0 to 13)
                node<ParadoxScopeValueNode>("some_target", 13 to 24) {
                    node<ParadoxDynamicValueExpression>("some_target", 13 to 24) {
                        node<ParadoxDynamicValueNode>("some_target", 13 to 24)
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
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxScopeNode>("owner@x.y", 5 to 14)
        }
        exp.check(dsl)
    }

    @Test
    fun testBarrier_Pipe_NoFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxScopeNode>("owner|x.y", 5 to 14)
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("", 0 to 0) {
            node<ParadoxErrorScopeNode>("", 0 to 0)
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments() {
        val s = "root.test_scope(root, some_building)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root, some_building)", 0 to 36) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("test_scope(root, some_building)", 5 to 36) {
                node<ParadoxScopePrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeValueNode>("root, some_building", 16 to 35) {
                    node<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                    node<ParadoxMarkerNode>(",", 20 to 21)
                    node<ParadoxBlankNode>(" ", 21 to 22)
                    node<ParadoxDataSourceNode>("some_building", 22 to 35)
                }
                node<ParadoxMarkerNode>(")", 35 to 36)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_building,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root, some_building,)", 0 to 37) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("test_scope(root, some_building,)", 5 to 37) {
                node<ParadoxScopePrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeValueNode>("root, some_building,", 16 to 36) {
                    node<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                    node<ParadoxMarkerNode>(",", 20 to 21)
                    node<ParadoxBlankNode>(" ", 21 to 22)
                    node<ParadoxDataSourceNode>("some_building", 22 to 35)
                    node<ParadoxMarkerNode>(",", 35 to 36)
                }
                node<ParadoxMarkerNode>(")", 36 to 37)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root)", 0 to 21) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("test_scope(root)", 5 to 21) {
                node<ParadoxScopePrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeValueNode>("root", 16 to 20) {
                    node<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                }
                node<ParadoxMarkerNode>(")", 20 to 21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root,)", 0 to 22) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("test_scope(root,)", 5 to 22) {
                node<ParadoxScopePrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeValueNode>("root,", 16 to 21) {
                    node<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                    node<ParadoxMarkerNode>(",", 20 to 21)
                }
                node<ParadoxMarkerNode>(")", 21 to 22)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_building)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(, some_building)", 0 to 32) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("test_scope(, some_building)", 5 to 32) {
                node<ParadoxScopePrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeValueNode>(", some_building", 16 to 31) {
                    node<ParadoxErrorTokenNode>("", 16 to 16)
                    node<ParadoxMarkerNode>(",", 16 to 17)
                    node<ParadoxBlankNode>(" ", 17 to 18)
                    node<ParadoxDataSourceNode>("some_building", 18 to 31)
                }
                node<ParadoxMarkerNode>(")", 31 to 32)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_for_Arguments_withLiteral() {
        val s = "root.test_literal_scope('foo bar', some_variable)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_literal_scope('foo bar', some_variable)", 0 to 49) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("test_literal_scope('foo bar', some_variable)", 5 to 49) {
                node<ParadoxScopePrefixNode>("test_literal_scope", 5 to 23)
                node<ParadoxMarkerNode>("(", 23 to 24)
                node<ParadoxScopeValueNode>("'foo bar', some_variable", 24 to 48) {
                    node<ParadoxStringLiteralNode>("'foo bar'", 24 to 33)
                    node<ParadoxMarkerNode>(",", 33 to 34)
                    node<ParadoxBlankNode>(" ", 34 to 35)
                    node<ParadoxDynamicValueExpression>("some_variable", 35 to 48) {
                        node<ParadoxDynamicValueNode>("some_variable", 35 to 48)
                    }
                }
                node<ParadoxMarkerNode>(")", 48 to 49)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_usePipeSeparator() {
        val s = "colonial_charter_utility(scope:target|scope:some)"
        val exp = parse(s, gameType = ParadoxGameType.Vic3)!! // ensure `scope:` is available
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("colonial_charter_utility(scope:target|scope:some)", 0 to 49) {
            node<ParadoxDynamicScopeNode>("colonial_charter_utility(scope:target|scope:some)", 0 to 49) {
                node<ParadoxScopePrefixNode>("colonial_charter_utility", 0 to 24)
                node<ParadoxMarkerNode>("(", 24 to 25)
                node<ParadoxScopeValueNode>("scope:target|scope:some", 25 to 48) {
                    node<ParadoxScopeFieldExpression>("scope:target", 25 to 37) {
                        node<ParadoxDynamicScopeNode>("scope:target", 25 to 37) {
                            node<ParadoxScopePrefixNode>("scope:", 25 to 31)
                            node<ParadoxScopeValueNode>("target", 31 to 37) {
                                node<ParadoxDynamicValueExpression>("target", 31 to 37) {
                                    node<ParadoxDynamicValueNode>("target", 31 to 37)
                                }
                            }
                        }
                    }
                    node<ParadoxMarkerNode>("|", 37 to 38)
                    node<ParadoxScopeFieldExpression>("scope:some", 38 to 48) {
                        node<ParadoxDynamicScopeNode>("scope:some", 38 to 48) {
                            node<ParadoxScopePrefixNode>("scope:", 38 to 44)
                            node<ParadoxScopeValueNode>("some", 44 to 48) {
                                node<ParadoxDynamicValueExpression>("some", 44 to 48) {
                                    node<ParadoxDynamicValueNode>("some", 44 to 48)
                                }
                            }
                        }
                    }
                }
                node<ParadoxMarkerNode>(")", 48 to 49)
            }
        }
        exp.check(dsl)
    }
}
