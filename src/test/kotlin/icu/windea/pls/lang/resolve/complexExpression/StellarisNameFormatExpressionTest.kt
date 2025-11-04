package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.expression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatClosureNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatDefinitionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatLocalisationNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNamePartNode
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class StellarisNameFormatExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = PlsTestUtil.initConfigGroups(project, ParadoxGameType.Stellaris)

    private fun parse(
        text: String,
        formatName: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): StellarisNameFormatExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        val cfg = CwtValueConfig.create(emptyPointer(), configGroup, "stellaris_name_format[$formatName]")
        return StellarisNameFormatExpression.resolve(text, TextRange(0, text.length), configGroup, cfg)
    }

    @Test
    fun testBasic_empire1() {
        val s = "{<eater_adj> {<patron_noun>}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..29) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<eater_adj>", 1..12) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("eater_adj", 2..11)
                    node<ParadoxMarkerNode>(">", 11..12)
                }
                node<ParadoxBlankNode>(" ", 12..13)
                node<StellarisNameFormatClosureNode>("{<patron_noun>}", 13..28) {
                    node<ParadoxMarkerNode>("{", 13..14)
                    node<StellarisNamePartNode>("<patron_noun>", 14..27) {
                        node<ParadoxMarkerNode>("<", 14..15)
                        node<StellarisNameFormatDefinitionNode>("patron_noun", 15..26)
                        node<ParadoxMarkerNode>(">", 26..27)
                    }
                    node<ParadoxMarkerNode>("}", 27..28)
                }
                node<ParadoxMarkerNode>("}", 28..29)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_empire2() {
        val s = "{AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..58) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("AofB", 1..5)
                node<StellarisNameFormatClosureNode>("{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}", 5..57) {
                    node<ParadoxMarkerNode>("{", 5..6)
                    node<StellarisNamePartNode>("<imperial_mil>", 6..20) {
                        node<ParadoxMarkerNode>("<", 6..7)
                        node<StellarisNameFormatDefinitionNode>("imperial_mil", 7..19)
                        node<ParadoxMarkerNode>(">", 19..20)
                    }
                    node<ParadoxBlankNode>(" ", 20..21)
                    node<ParadoxCommandNode>("[This.GetCapitalSystemNameOrRandom]", 21..56) {
                        node<ParadoxMarkerNode>("[", 21..22)
                        expression<ParadoxCommandExpression>("This.GetCapitalSystemNameOrRandom", 22..55) {
                            node<ParadoxSystemCommandScopeNode>("This", 22..26)
                            node<ParadoxOperatorNode>(".", 26..27)
                            node<ParadoxPredefinedCommandFieldNode>("GetCapitalSystemNameOrRandom", 27..55)
                        }
                        node<ParadoxMarkerNode>("]", 55..56)
                    }
                    node<ParadoxMarkerNode>("}", 56..57)
                }
                node<ParadoxMarkerNode>("}", 57..58)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_empire3() {
        val s = "{<home_planet> Fleet}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>("{<home_planet> Fleet}", 0..21) {
            node<StellarisNameFormatClosureNode>("{<home_planet> Fleet}", 0..21) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<home_planet>", 1..14) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("home_planet", 2..13)
                    node<ParadoxMarkerNode>(">", 13..14)
                }
                node<ParadoxBlankNode>(" ", 14..15)
                node<StellarisNameFormatLocalisationNode>("Fleet", 15..20)
                node<ParadoxMarkerNode>("}", 20..21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_federation() {
        val s = "{<union_adj> Council}"
        val exp = parse(s, formatName = "federation")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>("{<union_adj> Council}", 0..21) {
            node<StellarisNameFormatClosureNode>("{<union_adj> Council}", 0..21) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<union_adj>", 1..12) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("union_adj", 2..11)
                    node<ParadoxMarkerNode>(">", 11..12)
                }
                node<ParadoxBlankNode>(" ", 12..13)
                node<StellarisNameFormatLocalisationNode>("Council", 13..20)
                node<ParadoxMarkerNode>("}", 20..21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", formatName = "empire", incomplete = false))
        val exp = parse("", formatName = "empire", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>("", 0..0)
        exp.check(dsl)
    }

    // Strict DSL checks — ensure Marker vs ErrorToken are strictly distinguished

    @Test
    fun testStrict_braces_empty() {
        val s = "{}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>("{}", 0..2) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxMarkerNode>("}", 1..2)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_localisation_simple() {
        val s = "{alpha}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..7) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("alpha", 1..6)
                node<ParadoxMarkerNode>("}", 6..7)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_definition_simple() {
        val s = "{<x> y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..7) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<x>", 1..4) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("x", 2..3)
                    node<ParadoxMarkerNode>(">", 3..4)
                }
                node<ParadoxBlankNode>(" ", 4..5)
                node<StellarisNameFormatLocalisationNode>("y", 5..6)
                node<ParadoxMarkerNode>("}", 6..7)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_command_simple() {
        val s = "{[Root.GetName]}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..16) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxCommandNode>("[Root.GetName]", 1..15) {
                    node<ParadoxMarkerNode>("[", 1..2)
                    expression<ParadoxCommandExpression>("Root.GetName", 2..14) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2..6)
                        node<ParadoxOperatorNode>(".", 6..7)
                        node<ParadoxPredefinedCommandFieldNode>("GetName", 7..14)
                    }
                    node<ParadoxMarkerNode>("]", 14..15)
                }
                node<ParadoxMarkerNode>("}", 15..16)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_nested_mixed() {
        val s = "{X{<Y> [Root.GetName]}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..23) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("X", 1..2)
                node<StellarisNameFormatClosureNode>("{<Y> [Root.GetName]}", 2..22) {
                    node<ParadoxMarkerNode>("{", 2..3)
                    node<StellarisNamePartNode>("<Y>", 3..6) {
                        node<ParadoxMarkerNode>("<", 3..4)
                        node<StellarisNameFormatDefinitionNode>("Y", 4..5)
                        node<ParadoxMarkerNode>(">", 5..6)
                    }
                    node<ParadoxBlankNode>(" ", 6..7)
                    node<ParadoxCommandNode>("[Root.GetName]", 7..21) {
                        node<ParadoxMarkerNode>("[", 7..8)
                        expression<ParadoxCommandExpression>("Root.GetName", 8..20) {
                            node<ParadoxSystemCommandScopeNode>("Root", 8..12)
                            node<ParadoxOperatorNode>(".", 12..13)
                            node<ParadoxPredefinedCommandFieldNode>("GetName", 13..20)
                        }
                        node<ParadoxMarkerNode>("]", 20..21)
                    }
                    node<ParadoxMarkerNode>("}", 21..22)
                }
                node<ParadoxMarkerNode>("}", 22..23)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_error_unmatched_angle() {
        // NOTE 顶层与闭包层的“空错误节点”逻辑是幂等的：若子层已在当前层末位放置了错误节点（例如 [ 未闭合导致的当前层末尾空错误），则闭包层不会再重复添加。

        val s = "{<abc"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..5) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<abc", 1..5) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("abc", 2..5)
                    node<ParadoxErrorTokenNode>("", 5..5)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_error_unmatched_bracket() {
        // NOTE 顶层与闭包层的“空错误节点”逻辑是幂等的：若子层已在当前层末位放置了错误节点（例如 [ 未闭合导致的当前层末尾空错误），则闭包层不会再重复添加。

        val s = "{[Root."
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..7) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxCommandNode>("[Root.", 1..7) {
                    node<ParadoxMarkerNode>("[", 1..2)
                    expression<ParadoxCommandExpression>("Root.", 2..7) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2..6)
                        node<ParadoxOperatorNode>(".", 6..7)
                        node<ParadoxDynamicCommandFieldNode>("", 7..7) {
                            node<ParadoxCommandFieldValueNode>("", 7..7) {
                                node<ParadoxDataSourceNode>("", 7..7)
                            }
                        }
                    }
                    node<ParadoxErrorTokenNode>("", 7..7)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_text_and_blanks() {
        val s = "{Alpha Beta}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..12) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("Alpha", 1..6)
                node<ParadoxBlankNode>(" ", 6..7)
                node<StellarisNameFormatLocalisationNode>("Beta", 7..11)
                node<ParadoxMarkerNode>("}", 11..12)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_top_level_blanks_and_closure() {
        val s = "   { <x> y  }   "
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<ParadoxBlankNode>("   ", 0..3)
            node<StellarisNameFormatClosureNode>("{ <x> y  }", 3..13) {
                node<ParadoxMarkerNode>("{", 3..4)
                node<ParadoxBlankNode>(" ", 4..5)
                node<StellarisNamePartNode>("<x>", 5..8) {
                    node<ParadoxMarkerNode>("<", 5..6)
                    node<StellarisNameFormatDefinitionNode>("x", 6..7)
                    node<ParadoxMarkerNode>(">", 7..8)
                }
                node<ParadoxBlankNode>(" ", 8..9)
                node<StellarisNameFormatLocalisationNode>("y", 9..10)
                node<ParadoxBlankNode>("  ", 10..12)
                node<ParadoxMarkerNode>("}", 12..13)
            }
            node<ParadoxBlankNode>("   ", 13..16)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_top_level_errors_and_closure() {
        val s = "foo { <x> y  } <bar> "
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<ParadoxErrorTokenNode>("foo", 0..3)
            node<ParadoxBlankNode>(" ", 3..4)
            node<StellarisNameFormatClosureNode>("{ <x> y  }", 4..14) {
                node<ParadoxMarkerNode>("{", 4..5)
                node<ParadoxBlankNode>(" ", 5..6)
                node<StellarisNamePartNode>("<x>", 6..9) {
                    node<ParadoxMarkerNode>("<", 6..7)
                    node<StellarisNameFormatDefinitionNode>("x", 7..8)
                    node<ParadoxMarkerNode>(">", 8..9)
                }
                node<ParadoxBlankNode>(" ", 9..10)
                node<StellarisNameFormatLocalisationNode>("y", 10..11)
                node<ParadoxBlankNode>("  ", 11..13)
                node<ParadoxMarkerNode>("}", 13..14)
            }
            node<ParadoxBlankNode>(" ", 14..15)
            node<ParadoxErrorTokenNode>("<bar>", 15..20)
            node<ParadoxBlankNode>(" ", 20..21)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_no_open_brace_whole_error() {
        val s = "<x> y} "
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<ParadoxErrorTokenNode>("<x> y}", 0..6)
            node<ParadoxBlankNode>(" ", 6..7)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_stray_close_in_closure() {
        val s = "{x> y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..6) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("x", 1..2)
                node<ParadoxErrorTokenNode>(">", 2..3)
                node<ParadoxBlankNode>(" ", 3..4)
                node<StellarisNameFormatLocalisationNode>("y", 4..5)
                node<ParadoxMarkerNode>("}", 5..6)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_extra_close_top_level() {
        val s = "{<x>> y}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>("{<x>> y}", 0..8) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<x>", 1..4) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("x", 2..3)
                    node<ParadoxMarkerNode>(">", 3..4)
                }
                node<ParadoxErrorTokenNode>(">", 4..5)
                node<ParadoxBlankNode>(" ", 5..6)
                node<StellarisNameFormatLocalisationNode>("y", 6..7)
                node<ParadoxMarkerNode>("}", 7..8)
            }
            node<ParadoxErrorTokenNode>("}", 8..9)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_empty_command_in_closure() {
        val s = "{[]}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..4) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxCommandNode>("[]", 1..3) {
                    node<ParadoxMarkerNode>("[", 1..2)
                    node<ParadoxErrorTokenNode>("", 2..2)
                    node<ParadoxMarkerNode>("]", 2..3)
                }
                node<ParadoxMarkerNode>("}", 3..4)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_empty_definition_in_closure() {
        val s = "{<>}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..4) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<>", 1..3) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<ParadoxErrorTokenNode>("", 2..2)
                    node<ParadoxMarkerNode>(">", 2..3)
                }
                node<ParadoxMarkerNode>("}", 3..4)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_unmatched_angle_stop_at_space() {
        val s = "{<abc y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..8) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<abc", 1..5) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("abc", 2..5)
                    node<ParadoxErrorTokenNode>("", 5..5)
                }
                node<ParadoxBlankNode>(" ", 5..6)
                node<StellarisNameFormatLocalisationNode>("y", 6..7)
                node<ParadoxMarkerNode>("}", 7..8)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_unmatched_bracket_stop_at_space() {
        val s = "{[Root. y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>("{[Root. y}", 0..10) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxCommandNode>("[Root.", 1..7) {
                    node<ParadoxMarkerNode>("[", 1..2)
                    expression<ParadoxCommandExpression>("Root.", 2..7) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2..6)
                        node<ParadoxOperatorNode>(".", 6..7)
                        node<ParadoxDynamicCommandFieldNode>("", 7..7) {
                            node<ParadoxCommandFieldValueNode>("", 7..7) {
                                node<ParadoxDataSourceNode>("", 7..7)
                            }
                        }
                    }
                    node<ParadoxErrorTokenNode>("", 7..7)
                }
                node<ParadoxBlankNode>(" ", 7..8)
                node<StellarisNameFormatLocalisationNode>("y", 8..9)
                node<ParadoxMarkerNode>("}", 9..10)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_stray_close_bracket_in_closure() {
        val s = "{x] y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..6) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("x", 1..2)
                node<ParadoxErrorTokenNode>("]", 2..3)
                node<ParadoxBlankNode>(" ", 3..4)
                node<StellarisNameFormatLocalisationNode>("y", 4..5)
                node<ParadoxMarkerNode>("}", 5..6)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_top_level_after_closure_text() {
        val s = "{x}y"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>("{x}", 0..3) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("x", 1..2)
                node<ParadoxMarkerNode>("}", 2..3)
            }
            node<ParadoxErrorTokenNode>("y", 3..4)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_braces_blank_inside() {
        val s = "{ }"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..3) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxBlankNode>(" ", 1..2)
                node<ParadoxMarkerNode>("}", 2..3)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_command_adjacent_no_space() {
        val s = "{x[Root.GetName]}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..17) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("x", 1..2)
                node<ParadoxCommandNode>("[Root.GetName]", 2..16) {
                    node<ParadoxMarkerNode>("[", 2..3)
                    expression<ParadoxCommandExpression>("Root.GetName", 3..15) {
                        node<ParadoxSystemCommandScopeNode>("Root", 3..7)
                        node<ParadoxOperatorNode>(".", 7..8)
                        node<ParadoxPredefinedCommandFieldNode>("GetName", 8..15)
                    }
                    node<ParadoxMarkerNode>("]", 15..16)
                }
                node<ParadoxMarkerNode>("}", 16..17)
            }
        }
        exp.check(dsl)
    }
}
