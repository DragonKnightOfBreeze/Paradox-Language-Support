package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandSuffixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxStringLiteralNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
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
class ParadoxCommandExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxCommandExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxCommandExpression.resolve(text, null, configGroup)
    }

    @Test
    fun testBasic() {
        val s = "Root.GetName"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>(s, 0 to s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxPredefinedCommandFieldNode>("GetName", 5 to 12)
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_chain_noSuffix() {
        val s = "Root.Owner.event_target:some_target.var"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>(s, 0 to s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxCommandScopeNode>("Owner", 5 to 10)
            node<ParadoxOperatorNode>(".", 10 to 11)
            node<ParadoxDynamicCommandScopeLinkNode>("event_target:some_target", 11 to 35) {
                node<ParadoxCommandScopeLinkPrefixNode>("event_target:", 11 to 24)
                node<ParadoxCommandScopeLinkValueNode>("some_target", 24 to 35) {
                    node<ParadoxDataSourceNode>("some_target", 24 to 35)
                }
            }
            node<ParadoxOperatorNode>(".", 35 to 36)
            node<ParadoxDynamicCommandFieldNode>("var", 36 to 39) {
                node<ParadoxCommandFieldValueNode>("var", 36 to 39) {
                    node<ParadoxDataSourceNode>("var", 36 to 39)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_endsWithDot() {
        val s = "Root."
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>(s, 0 to s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandFieldNode>("", 5 to 5) {
                node<ParadoxCommandFieldValueNode>("", 5 to 5) {
                    node<ParadoxDataSourceNode>("", 5 to 5)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_endsWithVar() {
        val s = "Root.Var"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>(s, 0 to s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandFieldNode>("Var", 5 to 8) {
                node<ParadoxCommandFieldValueNode>("Var", 5 to 8) {
                    node<ParadoxDataSourceNode>("Var", 5 to 8)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testWithSuffix_amp() {
        val s = "Root.GetName&L"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>(s, 0 to s.length) {
            node<ParadoxCommandScopeLinkNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxCommandFieldNode>("GetName", 5 to 12)
            node<ParadoxMarkerNode>("&", 12 to 13)
            node<ParadoxCommandSuffixNode>("L", 13 to 14)
        }
        exp.check(dsl)
    }

    @Test
    fun testWithSuffix_doubleColon() {
        val s = "Root.GetName::UPPER"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>(s, 0 to s.length) {
            node<ParadoxCommandScopeLinkNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxCommandFieldNode>("GetName", 5 to 12)
            node<ParadoxMarkerNode>("::", 12 to 14)
            node<ParadoxCommandSuffixNode>("UPPER", 14 to 19)
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("", 0 to 0) {
            node<ParadoxDynamicCommandFieldNode>("", 0 to 0) {
                node<ParadoxCommandFieldValueNode>("", 0 to 0) {
                    node<ParadoxDataSourceNode>("", 0 to 0)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments() {
        val s = "Root.TestScope(root, some_building).TestCommand(some_flag, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("Root.TestScope(root, some_building).TestCommand(some_flag, some_job)", 0 to 68) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(root, some_building)", 5 to 35) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5 to 14)
                node<ParadoxMarkerNode>("(", 14 to 15)
                node<ParadoxCommandScopeLinkValueNode>("root, some_building", 15 to 34) {
                    node<ParadoxDataSourceNode>("root", 15 to 19)
                    node<ParadoxMarkerNode>(",", 19 to 20)
                    node<ParadoxBlankNode>(" ", 20 to 21)
                    node<ParadoxDataSourceNode>("some_building", 21 to 34)
                }
                node<ParadoxMarkerNode>(")", 34 to 35)
            }
            node<ParadoxOperatorNode>(".", 35 to 36)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, some_job)", 36 to 68) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 36 to 47)
                node<ParadoxMarkerNode>("(", 47 to 48)
                node<ParadoxCommandFieldValueNode>("some_flag, some_job", 48 to 67) {
                    node<ParadoxDataSourceNode>("some_flag", 48 to 57)
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
        val s = "Root.TestScope(root, some_building,).TestCommand(some_flag, some_job,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("Root.TestScope(root, some_building,).TestCommand(some_flag, some_job,)", 0 to 70) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(root, some_building,)", 5 to 36) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5 to 14)
                node<ParadoxMarkerNode>("(", 14 to 15)
                node<ParadoxCommandScopeLinkValueNode>("root, some_building,", 15 to 35) {
                    node<ParadoxDataSourceNode>("root", 15 to 19)
                    node<ParadoxMarkerNode>(",", 19 to 20)
                    node<ParadoxBlankNode>(" ", 20 to 21)
                    node<ParadoxDataSourceNode>("some_building", 21 to 34)
                    node<ParadoxMarkerNode>(",", 34 to 35)
                }
                node<ParadoxMarkerNode>(")", 35 to 36)
            }
            node<ParadoxOperatorNode>(".", 36 to 37)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, some_job,)", 37 to 70) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 37 to 48)
                node<ParadoxMarkerNode>("(", 48 to 49)
                node<ParadoxCommandFieldValueNode>("some_flag, some_job,", 49 to 69) {
                    node<ParadoxDataSourceNode>("some_flag", 49 to 58)
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
        val s = "Root.TestScope(some_building).TestCommand(some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("Root.TestScope(some_building).TestCommand(some_job)", 0 to 51) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(some_building)", 5 to 29) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5 to 14)
                node<ParadoxMarkerNode>("(", 14 to 15)
                node<ParadoxCommandScopeLinkValueNode>("some_building", 15 to 28) {
                    node<ParadoxDataSourceNode>("some_building", 15 to 28)
                }
                node<ParadoxMarkerNode>(")", 28 to 29)
            }
            node<ParadoxOperatorNode>(".", 29 to 30)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_job)", 30 to 51) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 30 to 41)
                node<ParadoxMarkerNode>("(", 41 to 42)
                node<ParadoxCommandFieldValueNode>("some_job", 42 to 50) {
                    node<ParadoxDataSourceNode>("some_job", 42 to 50)
                }
                node<ParadoxMarkerNode>(")", 50 to 51)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "Root.TestScope(root, ).TestCommand(some_flag, )"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("Root.TestScope(root, ).TestCommand(some_flag, )", 0 to 47) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(root, )", 5 to 22) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5 to 14)
                node<ParadoxMarkerNode>("(", 14 to 15)
                node<ParadoxCommandScopeLinkValueNode>("root, ", 15 to 21) {
                    node<ParadoxDataSourceNode>("root", 15 to 19)
                    node<ParadoxMarkerNode>(",", 19 to 20)
                    node<ParadoxBlankNode>(" ", 20 to 21)
                }
                node<ParadoxMarkerNode>(")", 21 to 22)
            }
            node<ParadoxOperatorNode>(".", 22 to 23)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, )", 23 to 47) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 23 to 34)
                node<ParadoxMarkerNode>("(", 34 to 35)
                node<ParadoxCommandFieldValueNode>("some_flag, ", 35 to 46) {
                    node<ParadoxDataSourceNode>("some_flag", 35 to 44)
                    node<ParadoxMarkerNode>(",", 44 to 45)
                    node<ParadoxBlankNode>(" ", 45 to 46)
                }
                node<ParadoxMarkerNode>(")", 46 to 47)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "Root.TestScope(, some_building).TestCommand(, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("Root.TestScope(, some_building).TestCommand(, some_job)", 0 to 55) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(, some_building)", 5 to 31) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5 to 14)
                node<ParadoxMarkerNode>("(", 14 to 15)
                node<ParadoxCommandScopeLinkValueNode>(", some_building", 15 to 30) {
                    node<ParadoxErrorTokenNode>("", 15 to 15)
                    node<ParadoxMarkerNode>(",", 15 to 16)
                    node<ParadoxBlankNode>(" ", 16 to 17)
                    node<ParadoxDataSourceNode>("some_building", 17 to 30)
                }
                node<ParadoxMarkerNode>(")", 30 to 31)
            }
            node<ParadoxOperatorNode>(".", 31 to 32)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(, some_job)", 32 to 55) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 32 to 43)
                node<ParadoxMarkerNode>("(", 43 to 44)
                node<ParadoxCommandFieldValueNode>(", some_job", 44 to 54) {
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
        val s = "Root.TestLiteralScope('foo bar', some_variable).TestCommand(some_flag, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxCommandExpression>("Root.TestLiteralScope('foo bar', some_variable).TestCommand(some_flag, some_job)", 0 to 80) {
            node<ParadoxSystemCommandScopeNode>("Root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestLiteralScope('foo bar', some_variable)", 5 to 47) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestLiteralScope", 5 to 21)
                node<ParadoxMarkerNode>("(", 21 to 22)
                node<ParadoxCommandScopeLinkValueNode>("'foo bar', some_variable", 22 to 46) {
                    node<ParadoxStringLiteralNode>("'foo bar'", 22 to 31)
                    node<ParadoxMarkerNode>(",", 31 to 32)
                    node<ParadoxBlankNode>(" ", 32 to 33)
                    node<ParadoxDataSourceNode>("some_variable", 33 to 46)
                }
                node<ParadoxMarkerNode>(")", 46 to 47)
            }
            node<ParadoxOperatorNode>(".", 47 to 48)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, some_job)", 48 to 80) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 48 to 59)
                node<ParadoxMarkerNode>("(", 59 to 60)
                node<ParadoxCommandFieldValueNode>("some_flag, some_job", 60 to 79) {
                    node<ParadoxDataSourceNode>("some_flag", 60 to 69)
                    node<ParadoxMarkerNode>(",", 69 to 70)
                    node<ParadoxBlankNode>(" ", 70 to 71)
                    node<ParadoxDataSourceNode>("some_job", 71 to 79)
                }
                node<ParadoxMarkerNode>(")", 79 to 80)
            }
        }
        exp.check(dsl)
    }
}
