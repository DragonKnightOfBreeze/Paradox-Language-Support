package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
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
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxStringLiteralNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldValueNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
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
    fun setup() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Vic3)
    }

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxValueFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxValueFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun testTrigger() {
        val s = "trigger:some_trigger"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0 to s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0 to 20) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 0 to 8)
                node<ParadoxValueFieldValueNode>("some_trigger", 8 to 20) {
                    node<ParadoxDataSourceNode>("some_trigger", 8 to 20)
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
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0 to s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0 to 26) {
                node<ParadoxValueFieldPrefixNode>("value:", 0 to 6)
                node<ParadoxValueFieldValueNode>("some_sv|PARAM|VALUE|", 6 to 26) {
                    expression<ParadoxScriptValueExpression>("some_sv|PARAM|VALUE|", 6 to 26) {
                        node<ParadoxScriptValueNode>("some_sv", 6 to 13)
                        node<ParadoxMarkerNode>("|", 13 to 14)
                        node<ParadoxScriptValueArgumentNode>("PARAM", 14 to 19)
                        node<ParadoxMarkerNode>("|", 19 to 20)
                        node<ParadoxScriptValueArgumentValueNode>("VALUE", 20 to 25)
                        node<ParadoxMarkerNode>("|", 25 to 26)
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
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeLinkNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicValueFieldNode>("value:some_sv|A|B|.owner", 5 to 29) {
                node<ParadoxValueFieldPrefixNode>("value:", 5 to 11)
                node<ParadoxValueFieldValueNode>("some_sv|A|B|.owner", 11 to 29) {
                    expression<ParadoxScriptValueExpression>("some_sv|A|B|.owner", 11 to 29) {
                        node<ParadoxScriptValueNode>("some_sv", 11 to 18)
                        node<ParadoxMarkerNode>("|", 18 to 19)
                        node<ParadoxScriptValueArgumentNode>("A", 19 to 20)
                        node<ParadoxMarkerNode>("|", 20 to 21)
                        node<ParadoxScriptValueArgumentValueNode>("B", 21 to 22)
                        node<ParadoxMarkerNode>("|", 22 to 23)
                        node<ParadoxScriptValueArgumentNode>(".owner", 23 to 29)
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
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0 to s.length) {
            node<ParadoxDynamicValueFieldNode>("relations(root)", 0 to 15) {
                node<ParadoxValueFieldPrefixNode>("relations", 0 to 9)
                node<ParadoxMarkerNode>("(", 9 to 10)
                node<ParadoxValueFieldValueNode>("root", 10 to 14) {
                    expression<ParadoxScopeFieldExpression>("root", 10 to 14) {
                        node<ParadoxSystemScopeNode>("root", 10 to 14)
                    }
                }
                node<ParadoxMarkerNode>(")", 14 to 15)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testForArgument_nested() {
        val s = "relations(scope:some_scope)"
        val exp = parse(s, gameType = ParadoxGameType.Vic3)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("relations(scope:some_scope)", 0 to 27) {
            node<ParadoxDynamicValueFieldNode>("relations(scope:some_scope)", 0 to 27) {
                node<ParadoxValueFieldPrefixNode>("relations", 0 to 9)
                node<ParadoxMarkerNode>("(", 9 to 10)
                node<ParadoxValueFieldValueNode>("scope:some_scope", 10 to 26) {
                    expression<ParadoxScopeFieldExpression>("scope:some_scope", 10 to 26) {
                        node<ParadoxDynamicScopeLinkNode>("scope:some_scope", 10 to 26) {
                            node<ParadoxScopeLinkPrefixNode>("scope:", 10 to 16)
                            node<ParadoxScopeLinkValueNode>("some_scope", 16 to 26) {
                                expression<ParadoxDynamicValueExpression>("some_scope", 16 to 26) {
                                    node<ParadoxDynamicValueNode>("some_scope", 16 to 26)
                                }
                            }
                        }
                    }
                }
                node<ParadoxMarkerNode>(")", 26 to 27)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testVariable_inChain() {
        val s = "root.owner.some_variable"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeLinkNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxScopeLinkNode>("owner", 5 to 10)
            node<ParadoxOperatorNode>(".", 10 to 11)
            node<ParadoxDynamicValueFieldNode>("some_variable", 11 to 24) {
                node<ParadoxValueFieldValueNode>("some_variable", 11 to 24) {
                    expression<ParadoxDynamicValueExpression>("some_variable", 11 to 24) {
                        node<ParadoxDynamicValueNode>("some_variable", 11 to 24)
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
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("", 0 to 0) {
            node<ParadoxDynamicValueFieldNode>("", 0 to 0) {
                node<ParadoxValueFieldValueNode>("", 0 to 0) {
                    expression<ParadoxDynamicValueExpression>("", 0 to 0) {
                        node<ParadoxDynamicValueNode>("", 0 to 0)
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
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root, some_building).test_value(some_flag, some_job)", 0 to 68) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root, some_building)", 5 to 36) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeLinkValueNode>("root, some_building", 16 to 35) {
                    expression<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                    node<ParadoxMarkerNode>(",", 20 to 21)
                    node<ParadoxBlankNode>(" ", 21 to 22)
                    node<ParadoxDataSourceNode>("some_building", 22 to 35)
                }
                node<ParadoxMarkerNode>(")", 35 to 36)
            }
            node<ParadoxOperatorNode>(".", 36 to 37)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job)", 37 to 68) {
                node<ParadoxValueFieldPrefixNode>("test_value", 37 to 47)
                node<ParadoxMarkerNode>("(", 47 to 48)
                node<ParadoxValueFieldValueNode>("some_flag, some_job", 48 to 67) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 48 to 57) {
                        node<ParadoxDynamicValueNode>("some_flag", 48 to 57)
                    }
                    node<ParadoxMarkerNode>(",", 57 to 58)
                    node<ParadoxBlankNode>(" ", 58 to 59)
                    node<ParadoxDataSourceNode>("some_job", 59 to 67)
                }
                node<ParadoxMarkerNode>(")", 67 to 68)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_building,).test_value(some_flag, some_job,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root, some_building,).test_value(some_flag, some_job,)", 0 to 70) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root, some_building,)", 5 to 37) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeLinkValueNode>("root, some_building,", 16 to 36) {
                    expression<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                    node<ParadoxMarkerNode>(",", 20 to 21)
                    node<ParadoxBlankNode>(" ", 21 to 22)
                    node<ParadoxDataSourceNode>("some_building", 22 to 35)
                    node<ParadoxMarkerNode>(",", 35 to 36)
                }
                node<ParadoxMarkerNode>(")", 36 to 37)
            }
            node<ParadoxOperatorNode>(".", 37 to 38)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job,)", 38 to 70) {
                node<ParadoxValueFieldPrefixNode>("test_value", 38 to 48)
                node<ParadoxMarkerNode>("(", 48 to 49)
                node<ParadoxValueFieldValueNode>("some_flag, some_job,", 49 to 69) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 49 to 58) {
                        node<ParadoxDynamicValueNode>("some_flag", 49 to 58)
                    }
                    node<ParadoxMarkerNode>(",", 58 to 59)
                    node<ParadoxBlankNode>(" ", 59 to 60)
                    node<ParadoxDataSourceNode>("some_job", 60 to 68)
                    node<ParadoxMarkerNode>(",", 68 to 69)
                }
                node<ParadoxMarkerNode>(")", 69 to 70)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root).test_value(some_flag)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root).test_value(some_flag)", 0 to 43) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root)", 5 to 21) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeLinkValueNode>("root", 16 to 20) {
                    expression<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                }
                node<ParadoxMarkerNode>(")", 20 to 21)
            }
            node<ParadoxOperatorNode>(".", 21 to 22)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag)", 22 to 43) {
                node<ParadoxValueFieldPrefixNode>("test_value", 22 to 32)
                node<ParadoxMarkerNode>("(", 32 to 33)
                node<ParadoxValueFieldValueNode>("some_flag", 33 to 42) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 33 to 42) {
                        node<ParadoxDynamicValueNode>("some_flag", 33 to 42)
                    }
                }
                node<ParadoxMarkerNode>(")", 42 to 43)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root,).test_value(some_flag,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root,).test_value(some_flag,)", 0 to 45) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(root,)", 5 to 22) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeLinkValueNode>("root,", 16 to 21) {
                    expression<ParadoxScopeFieldExpression>("root", 16 to 20) {
                        node<ParadoxSystemScopeNode>("root", 16 to 20)
                    }
                    node<ParadoxMarkerNode>(",", 20 to 21)
                }
                node<ParadoxMarkerNode>(")", 21 to 22)
            }
            node<ParadoxOperatorNode>(".", 22 to 23)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag,)", 23 to 45) {
                node<ParadoxValueFieldPrefixNode>("test_value", 23 to 33)
                node<ParadoxMarkerNode>("(", 33 to 34)
                node<ParadoxValueFieldValueNode>("some_flag,", 34 to 44) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 34 to 43) {
                        node<ParadoxDynamicValueNode>("some_flag", 34 to 43)
                    }
                    node<ParadoxMarkerNode>(",", 43 to 44)
                }
                node<ParadoxMarkerNode>(")", 44 to 45)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_building).test_value(, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(, some_building).test_value(, some_job)", 0 to 55) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeLinkNode>("test_scope(, some_building)", 5 to 32) {
                node<ParadoxScopeLinkPrefixNode>("test_scope", 5 to 15)
                node<ParadoxMarkerNode>("(", 15 to 16)
                node<ParadoxScopeLinkValueNode>(", some_building", 16 to 31) {
                    node<ParadoxErrorTokenNode>("", 16 to 16)
                    node<ParadoxMarkerNode>(",", 16 to 17)
                    node<ParadoxBlankNode>(" ", 17 to 18)
                    node<ParadoxDataSourceNode>("some_building", 18 to 31)
                }
                node<ParadoxMarkerNode>(")", 31 to 32)
            }
            node<ParadoxOperatorNode>(".", 32 to 33)
            node<ParadoxDynamicValueFieldNode>("test_value(, some_job)", 33 to 55) {
                node<ParadoxValueFieldPrefixNode>("test_value", 33 to 43)
                node<ParadoxMarkerNode>("(", 43 to 44)
                node<ParadoxValueFieldValueNode>(", some_job", 44 to 54) {
                    node<ParadoxErrorTokenNode>("", 44 to 44)
                    node<ParadoxMarkerNode>(",", 44 to 45)
                    node<ParadoxBlankNode>(" ", 45 to 46)
                    node<ParadoxDataSourceNode>("some_job", 46 to 54)
                }
                node<ParadoxMarkerNode>(")", 54 to 55)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_for_Arguments_withLiteral() {
        val s = "root.test_literal_scope('foo bar', some_variable).test_value(some_flag, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_literal_scope('foo bar', some_variable).test_value(some_flag, some_job)", 0 to 81) {
            node<ParadoxSystemScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeLinkNode>("test_literal_scope('foo bar', some_variable)", 5 to 49) {
                node<ParadoxScopeLinkPrefixNode>("test_literal_scope", 5 to 23)
                node<ParadoxMarkerNode>("(", 23 to 24)
                node<ParadoxScopeLinkValueNode>("'foo bar', some_variable", 24 to 48) {
                    node<ParadoxStringLiteralNode>("'foo bar'", 24 to 33)
                    node<ParadoxMarkerNode>(",", 33 to 34)
                    node<ParadoxBlankNode>(" ", 34 to 35)
                    expression<ParadoxDynamicValueExpression>("some_variable", 35 to 48) {
                        node<ParadoxDynamicValueNode>("some_variable", 35 to 48)
                    }
                }
                node<ParadoxMarkerNode>(")", 48 to 49)
            }
            node<ParadoxOperatorNode>(".", 49 to 50)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job)", 50 to 81) {
                node<ParadoxValueFieldPrefixNode>("test_value", 50 to 60)
                node<ParadoxMarkerNode>("(", 60 to 61)
                node<ParadoxValueFieldValueNode>("some_flag, some_job", 61 to 80) {
                    expression<ParadoxDynamicValueExpression>("some_flag", 61 to 70) {
                        node<ParadoxDynamicValueNode>("some_flag", 61 to 70)
                    }
                    node<ParadoxMarkerNode>(",", 70 to 71)
                    node<ParadoxBlankNode>(" ", 71 to 72)
                    node<ParadoxDataSourceNode>("some_job", 72 to 80)
                }
                node<ParadoxMarkerNode>(")", 80 to 81)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_usePipeSeparator() {
        val s = "colonial_charter_utility(scope:target|scope:some)"
        val exp = parse(s, gameType = ParadoxGameType.Vic3)!! // ensure `scope:` is available
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("colonial_charter_utility(scope:target|scope:some)", 0 to 49) {
            node<ParadoxDynamicValueFieldNode>("colonial_charter_utility(scope:target|scope:some)", 0 to 49) {
                node<ParadoxValueFieldPrefixNode>("colonial_charter_utility", 0 to 24)
                node<ParadoxMarkerNode>("(", 24 to 25)
                node<ParadoxValueFieldValueNode>("scope:target|scope:some", 25 to 48) {
                    expression<ParadoxScopeFieldExpression>("scope:target", 25 to 37) {
                        node<ParadoxDynamicScopeLinkNode>("scope:target", 25 to 37) {
                            node<ParadoxScopeLinkPrefixNode>("scope:", 25 to 31)
                            node<ParadoxScopeLinkValueNode>("target", 31 to 37) {
                                expression<ParadoxDynamicValueExpression>("target", 31 to 37) {
                                    node<ParadoxDynamicValueNode>("target", 31 to 37)
                                }
                            }
                        }
                    }
                    node<ParadoxMarkerNode>("|", 37 to 38)
                    expression<ParadoxScopeFieldExpression>("scope:some", 38 to 48) {
                        node<ParadoxDynamicScopeLinkNode>("scope:some", 38 to 48) {
                            node<ParadoxScopeLinkPrefixNode>("scope:", 38 to 44)
                            node<ParadoxScopeLinkValueNode>("some", 44 to 48) {
                                expression<ParadoxDynamicValueExpression>("some", 44 to 48) {
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
