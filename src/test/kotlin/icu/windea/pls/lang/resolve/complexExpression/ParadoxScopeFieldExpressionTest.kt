package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
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

    private fun resolve(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxScopeFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxScopeFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun test_singleScopeNode_root() {
        val s = "root"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0, s.length) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
        }
        exp.check(dsl)
    }

    @Test
    fun test_dotSegmentation_basic() {
        val s = "root.owner"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0, s.length) {
            node<ParadoxScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxScopeNode>("owner", 5, 10)
        }
        exp.check(dsl)
    }

    @Test
    fun test_eventTarget() {
        val s = "event_target:some_target"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0, s.length) {
            node<ParadoxDynamicScopeNode>("event_target:some_target", 0, 24) {
                node<ParadoxScopePrefixNode>("event_target:", 0, 13)
                node<ParadoxScopeValueNode>("some_target", 13, 24) {
                    node<ParadoxDynamicValueExpression>("some_target", 13, 24) {
                        node<ParadoxDynamicValueNode>("some_target", 13, 24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_barrier_Pipe_NoFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>(s, 0, s.length) {
            node<ParadoxScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxScopeNode>("owner|x.y", 5, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty_incompleteDiff() {
        Assert.assertNull(resolve("", incomplete = false))
        val exp = resolve("", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("", 0, 0) {
            node<ParadoxErrorScopeNode>("", 0, 0)
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments() {
        val s = "root.test_scope(root, some_building)"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root, some_building)", 0, 36) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("test_scope(root, some_building)", 5, 36) {
                node<ParadoxScopePrefixNode>("test_scope", 5, 15)
                node<ParadoxMarkerNode>("(", 15, 16)
                node<ParadoxScopeValueNode>("root, some_building", 16, 35) {
                    node<ParadoxScopeFieldExpression>("root", 16, 20) {
                        node<ParadoxSystemScopeNode>("root", 16, 20)
                    }
                    node<ParadoxMarkerNode>(",", 20, 21)
                    node<ParadoxBlankNode>(" ", 21, 22)
                    node<ParadoxDataSourceNode>("some_building", 22, 35)
                }
                node<ParadoxMarkerNode>(")", 35, 36)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_building,)"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root, some_building,)", 0, 37) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("test_scope(root, some_building,)", 5, 37) {
                node<ParadoxScopePrefixNode>("test_scope", 5, 15)
                node<ParadoxMarkerNode>("(", 15, 16)
                node<ParadoxScopeValueNode>("root, some_building,", 16, 36) {
                    node<ParadoxScopeFieldExpression>("root", 16, 20) {
                        node<ParadoxSystemScopeNode>("root", 16, 20)
                    }
                    node<ParadoxMarkerNode>(",", 20, 21)
                    node<ParadoxBlankNode>(" ", 21, 22)
                    node<ParadoxDataSourceNode>("some_building", 22, 35)
                    node<ParadoxMarkerNode>(",", 35, 36)
                }
                node<ParadoxMarkerNode>(")", 36, 37)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root)"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root)", 0, 21) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("test_scope(root)", 5, 21) {
                node<ParadoxScopePrefixNode>("test_scope", 5, 15)
                node<ParadoxMarkerNode>("(", 15, 16)
                node<ParadoxScopeValueNode>("root", 16, 20) {
                    node<ParadoxScopeFieldExpression>("root", 16, 20) {
                        node<ParadoxSystemScopeNode>("root", 16, 20)
                    }
                }
                node<ParadoxMarkerNode>(")", 20, 21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root,)"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(root,)", 0, 22) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("test_scope(root,)", 5, 22) {
                node<ParadoxScopePrefixNode>("test_scope", 5, 15)
                node<ParadoxMarkerNode>("(", 15, 16)
                node<ParadoxScopeValueNode>("root,", 16, 21) {
                    node<ParadoxScopeFieldExpression>("root", 16, 20) {
                        node<ParadoxSystemScopeNode>("root", 16, 20)
                    }
                    node<ParadoxMarkerNode>(",", 20, 21)
                }
                node<ParadoxMarkerNode>(")", 21, 22)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_building)"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_scope(, some_building)", 0, 32) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("test_scope(, some_building)", 5, 32) {
                node<ParadoxScopePrefixNode>("test_scope", 5, 15)
                node<ParadoxMarkerNode>("(", 15, 16)
                node<ParadoxScopeValueNode>(", some_building", 16, 31) {
                    node<ParadoxErrorTokenNode>("", 16, 16)
                    node<ParadoxMarkerNode>(",", 16, 17)
                    node<ParadoxBlankNode>(" ", 17, 18)
                    node<ParadoxDataSourceNode>("some_building", 18, 31)
                }
                node<ParadoxMarkerNode>(")", 31, 32)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withLiteral() {
        val s = "root.test_literal_scope('foo bar', some_variable)"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("root.test_literal_scope('foo bar', some_variable)", 0, 49) {
            node<ParadoxSystemScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("test_literal_scope('foo bar', some_variable)", 5, 49) {
                node<ParadoxScopePrefixNode>("test_literal_scope", 5, 23)
                node<ParadoxMarkerNode>("(", 23, 24)
                node<ParadoxScopeValueNode>("'foo bar', some_variable", 24, 48) {
                    node<ParadoxStringLiteralNode>("'foo bar'", 24, 33)
                    node<ParadoxMarkerNode>(",", 33, 34)
                    node<ParadoxBlankNode>(" ", 34, 35)
                    node<ParadoxDynamicValueExpression>("some_variable", 35, 48) {
                        node<ParadoxDynamicValueNode>("some_variable", 35, 48)
                    }
                }
                node<ParadoxMarkerNode>(")", 48, 49)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_usePipeSeparator() {
        val s = "colonial_charter_utility(scope:target|scope:some)"
        val exp = resolve(s, gameType = ParadoxGameType.Vic3)!! // ensure `scope:` is available
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("colonial_charter_utility(scope:target|scope:some)", 0, 49) {
            node<ParadoxDynamicScopeNode>("colonial_charter_utility(scope:target|scope:some)", 0, 49) {
                node<ParadoxScopePrefixNode>("colonial_charter_utility", 0, 24)
                node<ParadoxMarkerNode>("(", 24, 25)
                node<ParadoxScopeValueNode>("scope:target|scope:some", 25, 48) {
                    node<ParadoxScopeFieldExpression>("scope:target", 25, 37) {
                        node<ParadoxDynamicScopeNode>("scope:target", 25, 37) {
                            node<ParadoxScopePrefixNode>("scope:", 25, 31)
                            node<ParadoxScopeValueNode>("target", 31, 37) {
                                node<ParadoxDynamicValueExpression>("target", 31, 37) {
                                    node<ParadoxDynamicValueNode>("target", 31, 37)
                                }
                            }
                        }
                    }
                    node<ParadoxMarkerNode>("|", 37, 38)
                    node<ParadoxScopeFieldExpression>("scope:some", 38, 48) {
                        node<ParadoxDynamicScopeNode>("scope:some", 38, 48) {
                            node<ParadoxScopePrefixNode>("scope:", 38, 44)
                            node<ParadoxScopeValueNode>("some", 44, 48) {
                                node<ParadoxDynamicValueExpression>("some", 44, 48) {
                                    node<ParadoxDynamicValueNode>("some", 44, 48)
                                }
                            }
                        }
                    }
                }
                node<ParadoxMarkerNode>(")", 48, 49)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_simple() {
        val s = "this.event_target:target"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("this.event_target:target", 0, 24) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target", 5, 24) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target", 18, 24) {
                    node<ParadoxDynamicValueExpression>("target", 18, 24) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope() {
        val s = "this.event_target:target@root"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("this.event_target:target@root", 0, 29) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5, 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@root", 18, 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18, 29) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxScopeFieldExpression>("root", 25, 29) {
                            node<ParadoxSystemScopeNode>("root", 25, 29)
                        }
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_in_middle() {
        val s = "this.event_target:target@root"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("this.event_target:target@root", 0, 29) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5, 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@root", 18, 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18, 29) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxScopeFieldExpression>("root", 25, 29) {
                            node<ParadoxSystemScopeNode>("root", 25, 29)
                        }
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_inMiddle() {
        val s = "this.event_target:target@root.owner"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("this.event_target:target@root.owner", 0, 35) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5, 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@root", 18, 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18, 29) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxScopeFieldExpression>("root", 25, 29) {
                            node<ParadoxSystemScopeNode>("root", 25, 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29, 30)
            node<ParadoxStaticScopeNode>("owner", 30, 35)
        }

        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt() {
        val s = "this.event_target:target@"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("this.event_target:target@", 0, 25) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@", 5, 25) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@", 18, 25) {
                    node<ParadoxDynamicValueExpression>("target@", 18, 25) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxErrorTokenNode>("", 25, 25)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt_inMiddle() {
        val s = "this.event_target:target@.owner"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxScopeFieldExpression>("this.event_target:target@.owner", 0, 31) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@", 5, 25) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@", 18, 25) {
                    node<ParadoxDynamicValueExpression>("target@", 18, 25) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxErrorTokenNode>("", 25, 25)
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 25, 26)
            node<ParadoxStaticScopeNode>("owner", 26, 31)
        }
        exp.check(dsl)
    }
}
