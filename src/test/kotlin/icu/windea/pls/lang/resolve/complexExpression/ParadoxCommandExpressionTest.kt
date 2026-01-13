package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
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
        val dsl = buildExpression<ParadoxCommandExpression>(s, 0..s.length) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxPredefinedCommandFieldNode>("GetName", 5..12)
        }
        exp.check(dsl)
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    fun test_forArguments() {
        val s = "Root.TestScope(root, some_building).TestCommand(some_flag, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("Root.TestScope(root, some_building).TestCommand(some_flag, some_job)", 0..68) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(root, some_building)", 5..35) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5..14)
                node<ParadoxMarkerNode>("(", 14..15)
                node<ParadoxCommandScopeLinkValueNode>("root, some_building", 15..34) {
                    node<ParadoxDataSourceNode>("root", 15..19)
                    node<ParadoxMarkerNode>(",", 19..20)
                    node<ParadoxBlankNode>(" ", 20..21)
                    node<ParadoxDataSourceNode>("some_building", 21..34)
                }
                node<ParadoxMarkerNode>(")", 34..35)
            }
            node<ParadoxOperatorNode>(".", 35..36)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, some_job)", 36..68) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 36..47)
                node<ParadoxMarkerNode>("(", 47..48)
                node<ParadoxCommandFieldValueNode>("some_flag, some_job", 48..67) {
                    node<ParadoxDataSourceNode>("some_flag", 48..57)
                    node<ParadoxMarkerNode>(",", 57..58)
                    node<ParadoxBlankNode>(" ", 58..59)
                    node<ParadoxDataSourceNode>("some_job", 59..67)
                }
                node<ParadoxMarkerNode>(")", 67..68)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_withTrailComma() {
        val s = "Root.TestScope(root, some_building,).TestCommand(some_flag, some_job,)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("Root.TestScope(root, some_building,).TestCommand(some_flag, some_job,)", 0..70) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(root, some_building,)", 5..36) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5..14)
                node<ParadoxMarkerNode>("(", 14..15)
                node<ParadoxCommandScopeLinkValueNode>("root, some_building,", 15..35) {
                    node<ParadoxDataSourceNode>("root", 15..19)
                    node<ParadoxMarkerNode>(",", 19..20)
                    node<ParadoxBlankNode>(" ", 20..21)
                    node<ParadoxDataSourceNode>("some_building", 21..34)
                    node<ParadoxMarkerNode>(",", 34..35)
                }
                node<ParadoxMarkerNode>(")", 35..36)
            }
            node<ParadoxOperatorNode>(".", 36..37)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, some_job,)", 37..70) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 37..48)
                node<ParadoxMarkerNode>("(", 48..49)
                node<ParadoxCommandFieldValueNode>("some_flag, some_job,", 49..69) {
                    node<ParadoxDataSourceNode>("some_flag", 49..58)
                    node<ParadoxMarkerNode>(",", 58..59)
                    node<ParadoxBlankNode>(" ", 59..60)
                    node<ParadoxDataSourceNode>("some_job", 60..68)
                    node<ParadoxMarkerNode>(",", 68..69)
                }
                node<ParadoxMarkerNode>(")", 69..70)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_1() {
        val s = "Root.TestScope(some_building).TestCommand(some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("Root.TestScope(some_building).TestCommand(some_job)", 0..51) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(some_building)", 5..29) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5..14)
                node<ParadoxMarkerNode>("(", 14..15)
                node<ParadoxCommandScopeLinkValueNode>("some_building", 15..28) {
                    node<ParadoxDataSourceNode>("some_building", 15..28)
                }
                node<ParadoxMarkerNode>(")", 28..29)
            }
            node<ParadoxOperatorNode>(".", 29..30)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_job)", 30..51) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 30..41)
                node<ParadoxMarkerNode>("(", 41..42)
                node<ParadoxCommandFieldValueNode>("some_job", 42..50) {
                    node<ParadoxDataSourceNode>("some_job", 42..50)
                }
                node<ParadoxMarkerNode>(")", 50..51)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_2() {
        val s = "Root.TestScope(root, ).TestCommand(some_flag, )"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("Root.TestScope(root, ).TestCommand(some_flag, )", 0..47) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(root, )", 5..22) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5..14)
                node<ParadoxMarkerNode>("(", 14..15)
                node<ParadoxCommandScopeLinkValueNode>("root, ", 15..21) {
                    node<ParadoxDataSourceNode>("root", 15..19)
                    node<ParadoxMarkerNode>(",", 19..20)
                    node<ParadoxBlankNode>(" ", 20..21)
                }
                node<ParadoxMarkerNode>(")", 21..22)
            }
            node<ParadoxOperatorNode>(".", 22..23)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, )", 23..47) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 23..34)
                node<ParadoxMarkerNode>("(", 34..35)
                node<ParadoxCommandFieldValueNode>("some_flag, ", 35..46) {
                    node<ParadoxDataSourceNode>("some_flag", 35..44)
                    node<ParadoxMarkerNode>(",", 44..45)
                    node<ParadoxBlankNode>(" ", 45..46)
                }
                node<ParadoxMarkerNode>(")", 46..47)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_forArguments_missingArgument_3() {
        val s = "Root.TestScope(, some_building).TestCommand(, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("Root.TestScope(, some_building).TestCommand(, some_job)", 0..55) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestScope(, some_building)", 5..31) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestScope", 5..14)
                node<ParadoxMarkerNode>("(", 14..15)
                node<ParadoxCommandScopeLinkValueNode>(", some_building", 15..30) {
                    node<ParadoxErrorTokenNode>("", 15..15)
                    node<ParadoxMarkerNode>(",", 15..16)
                    node<ParadoxBlankNode>(" ", 16..17)
                    node<ParadoxDataSourceNode>("some_building", 17..30)
                }
                node<ParadoxMarkerNode>(")", 30..31)
            }
            node<ParadoxOperatorNode>(".", 31..32)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(, some_job)", 32..55) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 32..43)
                node<ParadoxMarkerNode>("(", 43..44)
                node<ParadoxCommandFieldValueNode>(", some_job", 44..54) {
                    node<ParadoxErrorTokenNode>("", 44..44)
                    node<ParadoxMarkerNode>(",", 44..45)
                    node<ParadoxBlankNode>(" ", 45..46)
                    node<ParadoxDataSourceNode>("some_job", 46..54)
                }
                node<ParadoxMarkerNode>(")", 54..55)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_for_Arguments_withLiteral() {
        val s = "Root.TestLiteralScope('foo bar', some_variable).TestCommand(some_flag, some_job)"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxCommandExpression>("Root.TestLiteralScope('foo bar', some_variable).TestCommand(some_flag, some_job)", 0..80) {
            node<ParadoxSystemCommandScopeNode>("Root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDynamicCommandScopeLinkNode>("TestLiteralScope('foo bar', some_variable)", 5..47) {
                node<ParadoxCommandScopeLinkPrefixNode>("TestLiteralScope", 5..21)
                node<ParadoxMarkerNode>("(", 21..22)
                node<ParadoxCommandScopeLinkValueNode>("'foo bar', some_variable", 22..46) {
                    node<ParadoxStringLiteralNode>("'foo bar'", 22..31)
                    node<ParadoxMarkerNode>(",", 31..32)
                    node<ParadoxBlankNode>(" ", 32..33)
                    node<ParadoxDataSourceNode>("some_variable", 33..46)
                }
                node<ParadoxMarkerNode>(")", 46..47)
            }
            node<ParadoxOperatorNode>(".", 47..48)
            node<ParadoxDynamicCommandFieldNode>("TestCommand(some_flag, some_job)", 48..80) {
                node<ParadoxCommandFieldPrefixNode>("TestCommand", 48..59)
                node<ParadoxMarkerNode>("(", 59..60)
                node<ParadoxCommandFieldValueNode>("some_flag, some_job", 60..79) {
                    node<ParadoxDataSourceNode>("some_flag", 60..69)
                    node<ParadoxMarkerNode>(",", 69..70)
                    node<ParadoxBlankNode>(" ", 70..71)
                    node<ParadoxDataSourceNode>("some_job", 71..79)
                }
                node<ParadoxMarkerNode>(")", 79..80)
            }
        }
        exp.check(dsl)
    }
}
