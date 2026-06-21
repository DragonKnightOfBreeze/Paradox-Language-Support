package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.base.context.ChronicleThreadContext
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
class ParadoxValueFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Vic3)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxValueFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) ChronicleThreadContext.incompleteComplexExpression.set(true) else ChronicleThreadContext.incompleteComplexExpression.remove()
        return ParadoxValueFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun testTrigger() {
        val s = "trigger:some_trigger"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0, s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0, 20) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 0, 8)
                node<ParadoxValueFieldValueNode>("some_trigger", 8, 20) {
                    node<ParadoxDataSourceNode>("some_trigger", 8, 20)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_scriptValue_basic() {
        val s = "value:some_sv|PARAM|VALUE|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0, s.length) {
            node<ParadoxDynamicValueFieldNode>(s, 0, 26) {
                node<ParadoxValueFieldPrefixNode>("value:", 0, 6)
                node<ParadoxValueFieldValueNode>("some_sv|PARAM|VALUE|", 6, 26) {
                    node<ParadoxScriptValueExpression>("some_sv|PARAM|VALUE|", 6, 26) {
                        node<ParadoxScriptValueNode>("some_sv", 6, 13)
                        node<ParadoxMarkerNode>("|", 13, 14)
                        node<ParadoxScriptValueArgumentNode>("PARAM", 14, 19)
                        node<ParadoxMarkerNode>("|", 19, 20)
                        node<ParadoxScriptValueArgumentValueNode>("VALUE", 20, 25)
                        node<ParadoxMarkerNode>("|", 25, 26)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_scriptValue_inChain_withDotBefore_andBarrierAfter() {
        val s = "root.value:some_sv|A|B|.owner"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0, s.length) {
            node<ParadoxScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicValueFieldNode>("value:some_sv|A|B|.owner", 5, 29) {
                node<ParadoxValueFieldPrefixNode>("value:", 5, 11)
                node<ParadoxValueFieldValueNode>("some_sv|A|B|.owner", 11, 29) {
                    node<ParadoxScriptValueExpression>("some_sv|A|B|.owner", 11, 29) {
                        node<ParadoxScriptValueNode>("some_sv", 11, 18)
                        node<ParadoxMarkerNode>("|", 18, 19)
                        node<ParadoxScriptValueArgumentNode>("A", 19, 20)
                        node<ParadoxMarkerNode>("|", 20, 21)
                        node<ParadoxScriptValueArgumentValueNode>("B", 21, 22)
                        node<ParadoxMarkerNode>("|", 22, 23)
                        node<ParadoxScriptValueArgumentNode>(".owner", 23, 29)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_variable_inChain() {
        val s = "root.owner.some_variable"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0, s.length) {
            node<ParadoxScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxScopeNode>("owner", 5, 10)
            node<ParadoxOperatorNode>(".", 10, 11)
            node<ParadoxDynamicValueFieldNode>("some_variable", 11, 24) {
                node<ParadoxValueFieldValueNode>("some_variable", 11, 24) {
                    node<ParadoxDynamicValueExpression>("some_variable", 11, 24) {
                        node<ParadoxDynamicValueNode>("some_variable", 11, 24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("", 0, 0) {
            node<ParadoxDynamicValueFieldNode>("", 0, 0) {
                node<ParadoxValueFieldValueNode>("", 0, 0) {
                    node<ParadoxDynamicValueExpression>("", 0, 0) {
                        node<ParadoxDynamicValueNode>("", 0, 0)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArgument() {
        val s = "relations(root)"
        val exp = resolve(s, ParadoxGameType.Vic3)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>(s, 0, s.length) {
            node<ParadoxDynamicValueFieldNode>("relations(root)", 0, 15) {
                node<ParadoxValueFieldPrefixNode>("relations", 0, 9)
                node<ParadoxMarkerNode>("(", 9, 10)
                node<ParadoxValueFieldValueNode>("root", 10, 14) {
                    node<ParadoxScopeFieldExpression>("root", 10, 14) {
                        node<ParadoxSystemScopeNode>("root", 10, 14)
                    }
                }
                node<ParadoxMarkerNode>(")", 14, 15)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArgument_nested() {
        val s = "relations(scope:some_scope)"
        val exp = resolve(s, ParadoxGameType.Vic3)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("relations(scope:some_scope)", 0, 27) {
            node<ParadoxDynamicValueFieldNode>("relations(scope:some_scope)", 0, 27) {
                node<ParadoxValueFieldPrefixNode>("relations", 0, 9)
                node<ParadoxMarkerNode>("(", 9, 10)
                node<ParadoxValueFieldValueNode>("scope:some_scope", 10, 26) {
                    node<ParadoxScopeFieldExpression>("scope:some_scope", 10, 26) {
                        node<ParadoxDynamicScopeNode>("scope:some_scope", 10, 26) {
                            node<ParadoxScopePrefixNode>("scope:", 10, 16)
                            node<ParadoxScopeValueNode>("some_scope", 16, 26) {
                                node<ParadoxDynamicValueExpression>("some_scope", 16, 26) {
                                    node<ParadoxDynamicValueNode>("some_scope", 16, 26)
                                }
                            }
                        }
                    }
                }
                node<ParadoxMarkerNode>(")", 26, 27)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments() {
        val s = "root.test_scope(root, some_building).test_value(some_flag, some_job)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root, some_building).test_value(some_flag, some_job)", 0, 68) {
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
            node<ParadoxOperatorNode>(".", 36, 37)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job)", 37, 68) {
                node<ParadoxValueFieldPrefixNode>("test_value", 37, 47)
                node<ParadoxMarkerNode>("(", 47, 48)
                node<ParadoxValueFieldValueNode>("some_flag, some_job", 48, 67) {
                    node<ParadoxDynamicValueExpression>("some_flag", 48, 57) {
                        node<ParadoxDynamicValueNode>("some_flag", 48, 57)
                    }
                    node<ParadoxMarkerNode>(",", 57, 58)
                    node<ParadoxBlankNode>(" ", 58, 59)
                    node<ParadoxDataSourceNode>("some_job", 59, 67)
                }
                node<ParadoxMarkerNode>(")", 67, 68)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "root.test_scope(root, some_building,).test_value(some_flag, some_job,)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root, some_building,).test_value(some_flag, some_job,)", 0, 70) {
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
            node<ParadoxOperatorNode>(".", 37, 38)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job,)", 38, 70) {
                node<ParadoxValueFieldPrefixNode>("test_value", 38, 48)
                node<ParadoxMarkerNode>("(", 48, 49)
                node<ParadoxValueFieldValueNode>("some_flag, some_job,", 49, 69) {
                    node<ParadoxDynamicValueExpression>("some_flag", 49, 58) {
                        node<ParadoxDynamicValueNode>("some_flag", 49, 58)
                    }
                    node<ParadoxMarkerNode>(",", 58, 59)
                    node<ParadoxBlankNode>(" ", 59, 60)
                    node<ParadoxDataSourceNode>("some_job", 60, 68)
                    node<ParadoxMarkerNode>(",", 68, 69)
                }
                node<ParadoxMarkerNode>(")", 69, 70)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "root.test_scope(root).test_value(some_flag)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root).test_value(some_flag)", 0, 43) {
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
            node<ParadoxOperatorNode>(".", 21, 22)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag)", 22, 43) {
                node<ParadoxValueFieldPrefixNode>("test_value", 22, 32)
                node<ParadoxMarkerNode>("(", 32, 33)
                node<ParadoxValueFieldValueNode>("some_flag", 33, 42) {
                    node<ParadoxDynamicValueExpression>("some_flag", 33, 42) {
                        node<ParadoxDynamicValueNode>("some_flag", 33, 42)
                    }
                }
                node<ParadoxMarkerNode>(")", 42, 43)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "root.test_scope(root,).test_value(some_flag,)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(root,).test_value(some_flag,)", 0, 45) {
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
            node<ParadoxOperatorNode>(".", 22, 23)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag,)", 23, 45) {
                node<ParadoxValueFieldPrefixNode>("test_value", 23, 33)
                node<ParadoxMarkerNode>("(", 33, 34)
                node<ParadoxValueFieldValueNode>("some_flag,", 34, 44) {
                    node<ParadoxDynamicValueExpression>("some_flag", 34, 43) {
                        node<ParadoxDynamicValueNode>("some_flag", 34, 43)
                    }
                    node<ParadoxMarkerNode>(",", 43, 44)
                }
                node<ParadoxMarkerNode>(")", 44, 45)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "root.test_scope(, some_building).test_value(, some_job)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_scope(, some_building).test_value(, some_job)", 0, 55) {
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
            node<ParadoxOperatorNode>(".", 32, 33)
            node<ParadoxDynamicValueFieldNode>("test_value(, some_job)", 33, 55) {
                node<ParadoxValueFieldPrefixNode>("test_value", 33, 43)
                node<ParadoxMarkerNode>("(", 43, 44)
                node<ParadoxValueFieldValueNode>(", some_job", 44, 54) {
                    node<ParadoxErrorTokenNode>("", 44, 44)
                    node<ParadoxMarkerNode>(",", 44, 45)
                    node<ParadoxBlankNode>(" ", 45, 46)
                    node<ParadoxDataSourceNode>("some_job", 46, 54)
                }
                node<ParadoxMarkerNode>(")", 54, 55)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withLiteral() {
        val s = "root.test_literal_scope('foo bar', some_variable).test_value(some_flag, some_job)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("root.test_literal_scope('foo bar', some_variable).test_value(some_flag, some_job)", 0, 81) {
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
            node<ParadoxOperatorNode>(".", 49, 50)
            node<ParadoxDynamicValueFieldNode>("test_value(some_flag, some_job)", 50, 81) {
                node<ParadoxValueFieldPrefixNode>("test_value", 50, 60)
                node<ParadoxMarkerNode>("(", 60, 61)
                node<ParadoxValueFieldValueNode>("some_flag, some_job", 61, 80) {
                    node<ParadoxDynamicValueExpression>("some_flag", 61, 70) {
                        node<ParadoxDynamicValueNode>("some_flag", 61, 70)
                    }
                    node<ParadoxMarkerNode>(",", 70, 71)
                    node<ParadoxBlankNode>(" ", 71, 72)
                    node<ParadoxDataSourceNode>("some_job", 72, 80)
                }
                node<ParadoxMarkerNode>(")", 80, 81)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_usePipeSeparator() {
        val s = "colonial_charter_utility(scope:target|scope:some)"
        val exp = resolve(s, ParadoxGameType.Vic3)!! // ensure `scope:` is available
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("colonial_charter_utility(scope:target|scope:some)", 0, 49) {
            node<ParadoxDynamicValueFieldNode>("colonial_charter_utility(scope:target|scope:some)", 0, 49) {
                node<ParadoxValueFieldPrefixNode>("colonial_charter_utility", 0, 24)
                node<ParadoxMarkerNode>("(", 24, 25)
                node<ParadoxValueFieldValueNode>("scope:target|scope:some", 25, 48) {
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
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("this.event_target:target", 0, 24) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicValueFieldNode>("event_target:target", 5, 24) {
                node<ParadoxValueFieldValueNode>("event_target:target", 5, 24) {
                    node<ParadoxDynamicValueExpression>("event_target:target", 5, 24) {
                        node<ParadoxDynamicValueNode>("event_target:target", 5, 24)
                    }
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope() {
        val s = "this.event_target:target@root.trigger:x"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("this.event_target:target@root.trigger:x", 0, 39) {
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
            node<ParadoxDynamicValueFieldNode>("trigger:x", 30, 39) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 30, 38)
                node<ParadoxValueFieldValueNode>("x", 38, 39) {
                    node<ParadoxDataSourceNode>("x", 38, 39)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_in_middle() {
        val s = "this.event_target:target@root.trigger:x"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("this.event_target:target@root.trigger:x", 0, 39) {
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
            node<ParadoxDynamicValueFieldNode>("trigger:x", 30, 39) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 30, 38)
                node<ParadoxValueFieldValueNode>("x", 38, 39) {
                    node<ParadoxDataSourceNode>("x", 38, 39)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_inMiddle() {
        val s = "this.event_target:target@root.owner.trigger:x"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("this.event_target:target@root.owner.trigger:x", 0, 45) {
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
            node<ParadoxOperatorNode>(".", 35, 36)
            node<ParadoxDynamicValueFieldNode>("trigger:x", 36, 45) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 36, 44)
                node<ParadoxValueFieldValueNode>("x", 44, 45) {
                    node<ParadoxDataSourceNode>("x", 44, 45)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt() {
        val s = "this.event_target:target@.trigger:x"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("this.event_target:target@.trigger:x", 0, 35) {
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
            node<ParadoxDynamicValueFieldNode>("trigger:x", 26, 35) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 26, 34)
                node<ParadoxValueFieldValueNode>("x", 34, 35) {
                    node<ParadoxDataSourceNode>("x", 34, 35)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt_inMiddle() {
        val s = "this.event_target:target@.owner.trigger:x"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxValueFieldExpression>("this.event_target:target@.owner.trigger:x", 0, 41) {
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
            node<ParadoxOperatorNode>(".", 31, 32)
            node<ParadoxDynamicValueFieldNode>("trigger:x", 32, 41) {
                node<ParadoxValueFieldPrefixNode>("trigger:", 32, 40)
                node<ParadoxValueFieldValueNode>("x", 40, 41) {
                    node<ParadoxDataSourceNode>("x", 40, 41)
                }
            }
        }
        exp.check(dsl)
    }
}
